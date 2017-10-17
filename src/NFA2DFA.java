import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import javax.swing.JOptionPane;


/**
 * 实现NFA到DFA的转换
 * 算法：子集法
 * 其中NFA的状态集使用Set存储
 * 具体使用LinkedHashSet而不是HashSet，
 * 因为LinkedHashSet元素取出数据和插入数据一致，
 * 可保证程序结果和手算结果相同
 * （虽然HashSet的结果等价，但有些需要新的对应关系）
 * 
 * 输入输出均为状态矩阵
 * 输入NFA状态矩阵stateOfNFA的格式为：
 * Q\E a b e_null
 * 1 4,5 s_null 2
 * 2 s_null 3 s_null
 * 3 s_null s_null 8
 * 4 s_null s_null 7
 * 5 s_null s_null 6
 * 6* s_null 9 2
 * 7* s_null 9 s_null
 * 8 9 s_null s_null
 * 9* s_null s_null s_null
 * 其中'e_null'表示空串e,'s_null'表示空集
 * 第一行表示字母表中的字母，第一列表示状态集中的状态（不是集合）
 * 此NFA在书P37
 * 
 * NFA确定化过程构造的状态表stateN2D的格式为：
 * I			Ia			Ib
 * 1,2			5,4,6,7,2	3,8
 * 5,4,6,7,2,*	s_null		3,9,8
 * 3,8,*		9			s_null
 * 3,9,8		9			s_null
 * 9,*			s_null		s_null
 * 其中's_null'表示空集
 * 第一行表示Ia为I的e闭包的a弧e闭包...，第一列表示新的状态集
 * 写入文件时s_null表示为[]
 * 
 * 输入文件为NFA.txt（虽然可以用JOptionPane很轻松的自由输入，但是。。。再说吧）
 * 生成中间状态表文件MiddleN2D.txt
 * 生成最终DFA文件GeneDFA.txt
 * 生成中间状态表中的状态到DFA状态的转换过程文件TransState.txt
 * @author 北京理工大学	1120122018	吴一凡
 *
 */
public class NFA2DFA {
	private final int MAX = 100;//最多有100*100的状态矩阵
	private State[][] stateOfNFA = new State[MAX][MAX];//需要在构造方法里new分配内存
	private String[][] stateOfDFA = new String[MAX][MAX];
	private State[][] stateN2D = new State[MAX][MAX];//NFA确定化过程构造的状态表
	private int row, col;//行列计数
	private int realRow, realCol;//实际输入的行列数
	
	NFA2DFA(){
		for(int i = 0; i<MAX; i++){
			for(int j = 0; j<MAX; j++){
				stateOfNFA[i][j] = new State();
				stateN2D[i][j] = new State();
				stateOfDFA[i][j] = new String();
			}
		}
	}
	private class State{	
		/*由于不能定义泛型数组，即不能有Set<String>[]的定义形式，故用一个简单的类State来表示NFA的
		 * 状态，从而达到Set<String>[]的类似目的
		 */
		Set<String> state = new LinkedHashSet<String>();
	}
	private Set<String> eee_Closure(Set<String> eSta){//e闭包，并解决eee...e=e的情况
		Set<String> e_cls = new LinkedHashSet<String>();//一次e闭包状态集
		Set<String> e_cls2nd = new LinkedHashSet<String>();//两次e闭包状态集
//		int count = 1;//计数
		e_cls = e_Closure(eSta);
		e_cls2nd = e_Closure(e_cls);//通过递归调用，解决eee...e=e这样重复多次e的情况
		while(!e_cls2nd.equals(e_cls)){//不相等时继续向下求e闭包
//			System.out.println("count = " + ++count);
			e_cls = e_Closure(e_cls2nd);
			e_cls2nd = e_Closure(e_cls);
		}
		return e_cls2nd;
//		if(e_cls2nd.equals(e_cls)){
//			return e_cls;
//		} else {
//			return e_cls2nd;
//		}
	}
	private Set<String> e_Closure(Set<String> eSta){//e闭包输入为一个状态集，输出为一个状态集
//		System.out.println("eSta = " + eSta);
		Set<String> e_cls = new LinkedHashSet<String>();//一次e闭包状态集
		Set<String> fqeSet = new LinkedHashSet<String>();//f(q,e)结果的集合
		int i, j;//记录f(q,e)结果在NFA状态表中的行列数
		int row, col;//遍历NFA状态表的指针
		Iterator<String> it = eSta.iterator();
		while(it.hasNext()){
			String q = it.next();
//			System.out.println("q = " + q);
			if(eSta.contains(q)){//若q∈I，则q∈e_closure(I);
				e_cls.add(q);
			//--------------寻找f(q,e)----------------
				i = j = 0;//初始化i,j
				for(row = 0; row < realRow; row++){//寻找q状态所在的那行
					//要注意状态后面可能会有*号，所以判断时需要判断q或者q+"*"是否在集合中
					if(stateOfNFA[row][0].state.contains(q)||stateOfNFA[row][0].state.contains(q+"*")){
						i = row;
						break;
					}
				}
				for(col = 0; col < realCol; col++){//寻找e_null所在的那列
					if(stateOfNFA[0][col].state.contains("e_null")){
						j = col;
						break;
					}
				}
//				System.out.println("i = " + i + ",j = " + j);
				fqeSet = stateOfNFA[i][j].state;
//				System.out.println(fqeSet);
				if(fqeSet.contains("s_null")){
					;
				}else {//若q∈I，则对任意q'属于f(q,e),有q'属于e_closure(I);
					Iterator<String> itq_ = fqeSet.iterator();
					while(itq_.hasNext()){
						String q_ = itq_.next();
						e_cls.add(q_);
					}
				}
			}
		}
		return e_cls;
	}
	private Set<String> Ia(Set<String> I, String a){//Ia为I的e闭包的a弧e闭包
		
		Set<String> eee_clsOfI = new LinkedHashSet<String>();//I的e闭包
		Set<String> fqaSet = new LinkedHashSet<String>();//f(q,a)结果的集合
		Set<String> fqaSetUnion = new LinkedHashSet<String>();//所有f(q,a)结果的集合的并集
		int i, j;//记录f(q,e)结果在NFA状态表中的行列数
		int row, col;//遍历NFA状态表的指针
		
		eee_clsOfI = eee_Closure(I);
//		System.out.println("eee_clsOfI = " + eee_clsOfI);
	//------------求q属于eee_closure(I)时的f(q,a)集合
		Iterator<String> it = eee_clsOfI.iterator();
		while(it.hasNext()){
			String q = it.next();//取出eee_closure(I)中的状态q
			i = j = 0;//初始化i,j
			for(row = 0; row < realRow; row++){//寻找q状态所在的那行
				//要注意状态后面可能会有*号，所以判断时需要判断q或者q+"*"是否在集合中
				if(stateOfNFA[row][0].state.contains(q)||stateOfNFA[row][0].state.contains(q+"*")){
					i = row;
					break;
				}
			}
			for(col = 0; col < realCol; col++){//寻找e_null所在的那列
				if(stateOfNFA[0][col].state.contains(a)){
					j = col;
					break;
				}
			}
			fqaSet = stateOfNFA[i][j].state;
			if(fqaSet.contains("s_null")){
			}else {
				fqaSetUnion.addAll(fqaSet);
			}
//			System.out.println("q = " + q);
//			System.out.println("fqaSet = " + fqaSet);
		}
		return eee_Closure(fqaSetUnion);
	}

	public void inputNFAState(State[][] sta) throws IOException{
		try {
			BufferedReader br = new BufferedReader(new FileReader(
					"NFA.txt"));
			String line = br.readLine();
			this.row = this.col = 0;	//初始化行列
//			System.out.println(line);
			while(line != null){
				Scanner scanner1 = new Scanner(line);//按空格和回车读出每个集合
				scanner1.useDelimiter("[ |\n]");
				while(scanner1.hasNext()){
					String s1 = scanner1.next();
//					System.out.println("scanner1: " + s1);
					Scanner scanner2 = new Scanner(s1);//读出每个集合内的元素
					scanner2.useDelimiter(",");
//					sta[row][col] = new State(); //不需要产生MAX多个
//					stateOfDFA[row][col] = new State();
					while(scanner2.hasNext()){
						String s2 = scanner2.next();
//						System.out.println("scanner2: " + s2);
//						System.out.println("row = " + row + ",col = " + col);
						sta[row][col].state.add(s2);
						
//						System.out.println("[" + row + "]" + "[" + col + "]" +
//										sta[row][col].state);
					}
//					System.out.println("size = " + sta[row][col].state.size());
//					Iterator<String> it = sta[row][col].state.iterator();
//					while(it.hasNext()){
//						String its = it.next();
//						System.out.println(its);
//					}
					col++;
					scanner2.close();
				}
				scanner1.close();
				row++;
				realCol = col;
				col = 0;
//				System.out.println("row=" + row + ",col=" + col);
				line = br.readLine();
			}
			realRow = row;
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void transform(){
//		System.out.println("eee_closure : " + eee_Closure(stateOfNFA[5][0].state));
//		System.out.println("Ia : " + Ia(stateOfNFA[1][0].state, "a"));
		Set<Set<String>> Q_ = new LinkedHashSet<Set<String>>();//Q'(Q_)将stateN2D的第一列摘出作为DFA的状态集
		Set<String> M = new LinkedHashSet<String>();//M为FA的字母表
		Set<String> tempSet = new LinkedHashSet<String>();//临时存放Ia，Ib的结果
		int row, col;//col为寻找e_null而生,row,col保存中间状态表的行列数，不可变
		boolean flag;//标志Q'中是否有元素的添加
		String[] dealed = new String[MAX];//保存处理过的状态
		boolean deal;//deal为false表示没有被处理过
		int count;//记录处理过的状态数
		int i, j;
//----------------------生成状态集Q'--------------------------------------	
		for(col = 1; col < realCol; col++){//将字母表从stateOfNFA中取出
			if(stateOfNFA[0][col].state.contains("e_null")){//字母表中去掉e_null
				;
			} else{
				M.addAll(stateOfNFA[0][col].state);
			}
		}
//		System.out.println("M = " + M);
		Q_.add(eee_Closure(stateOfNFA[1][0].state));//初始化Q'为e_closure{q0}
//		System.out.println("Q' init:" + Q_);	
		count = 0;
		Iterator<Set<String>> itQ_ = Q_.iterator();
		while(itQ_.hasNext()){//存在状态集X∈Q'
			flag = false;
			deal = false;
			Set<String> X = new LinkedHashSet<String>();
			X = itQ_.next();
//			System.out.println("X = " + X);
			
			for(i = 0; i < count; i++){
				if(dealed[i].equals(X.toString())){
					deal = true;
					break;
				}
			}
			if(deal){
				continue;
			} else{
				Iterator<String> itM = M.iterator();
				while(itM.hasNext()){//任意A∈M
					String a = itM.next();
//					System.out.println("a = " + a);
					tempSet = Ia(X, a);//计算对状态集X和输入字符a计算的Ia值
//					System.out.println("tempSet = " + tempSet);
					if(!tempSet.isEmpty()&&!Q_.contains(tempSet)){
						Q_.add(tempSet);
						flag = true;
					}
//					System.out.println("Q' = " + Q_);
				}
				if(flag){
					/*如果Q'中新加入过元素，则更新迭代器为新的Q'，否则会漏掉新加入的元素。
					 * 通过对dealed数组的判断得到状态是否处理过的信息，从而重复处理
					 */
					itQ_ = Q_.iterator();
				}
				dealed[count++] = X.toString();//将处理的状态加入dealed中
			}
		}
//		System.out.println("Q': " + Q_);
		
//-----------------------------生成中间过程状态表stateN2D-----------------------
		//stateN2D列数为realCol-1,少了e_null。行数从0向上加，直到Q'中无元素。
		col = 1;
		stateN2D[0][0].state.add("I");//左上角为I
		String[] rowNo1 = new String[MAX];//中间状态表的第一行的字母
		Iterator<String> itM = M.iterator();//遍历字母表,M中不含e_null
		//(不能用上面遍历时声明的itM，不在作用域内)
		while(itM.hasNext()){
			String itMS = itM.next();
			stateN2D[0][col].state.add(itMS);//第一行为Ia,Ib
//			System.out.println("stateN2D[0][col].state = " + stateN2D[0][col].state);
			rowNo1[col] = itMS;
			stateOfDFA[0][col] = itMS;
			col++;
		}
		row = 1;
		itQ_ = Q_.iterator();//遍历状态集
		Set<String> X = null;
		while(itQ_.hasNext()){
			X = itQ_.next();
			stateN2D[row][0].state.addAll(X);
			for(j = 1; j < col; j++){//按行向状态表中填入信息
//				System.out.println("X = " + X);
				stateN2D[row][j].state.addAll(Ia(X, rowNo1[j]));
				//toString方法，返回的字符串含有[]，不可用
//				System.out.println("row = " + row + ",j = " + j);
//				System.out.println("stateN2D[row][j].state = " + stateN2D[row][j].state);
			}
			row++;
		}
//-----------------------------生成DFA的中间部分内容--------------------
		//遍历中间状态表，寻找与新编号对应的状态集，将其映射到DFA表中。
		for(i = 1; i < row; i++){
			for(j = 1; j < col; j++){
				for(int k = 1; k < row; k++){
					if(stateN2D[i][j].state.isEmpty()){//中间状态表值为空，则在DFA中添加'/'
						stateOfDFA[i][j] = "/";
					}
					if(stateN2D[i][j].state.equals(stateN2D[k][0].state)){
						stateOfDFA[i][j] = new Integer(k).toString();
//						System.out.println(stateOfDFA[i][j]);
					}
				}
			}
		}
		
//----------------------------添加终结符号*---------------------------------
//		System.out.println("ending *");
	//先将原NFA中的终结状态找出来，去掉*号，看看在中间状态表中的那个状态集中，然后向其中加入*号
		//找出NFA中的终结状态
		Iterator<String> it = null;
		String q = null;
		String[] endSta = new String[MAX];//记录终结状态除去*号的字符串
//		int count;//endSta的计数器
		for(i = 1, count = 0; i < realRow; i++){
			it = stateOfNFA[i][0].state.iterator();
			while(it.hasNext()){
				q = it.next();
				if(q.endsWith("*")){//带*号的状态都以*号结尾
					endSta[count++] = q.substring(0, q.length()-1);
				}
			}
		}
		
		//遍历中间状态表中的状态集，添加*号
//		System.out.println("row = " + row);
		for(i = 1; i < row; i++){
			X = stateN2D[i][0].state;
			for(j = 0; j < endSta.length; j++){
				if(X.contains(endSta[j])&&!X.contains("*")){//包含终结符但没有*号
					stateN2D[i][0].state.add("*");
				}
			}
//			System.out.println("stateN2D[i][0].state = " + stateN2D[i][0].state);
		}
		
//-------------------------生成最终的DFA表----------------------------
	//DFA表的行与中间状态表的行内容相同，列为1，2，3...,如果是终结状态则后面加*号
		//为DFA的第一行第一列赋值
		stateOfDFA[0][0] = "head";
		//DFA第一行在将a,b...装入中间状态表时已经装入
		
		//DFA第一列为1，2，3...,如果是终结状态则后面加*号
		for(i = 1; i < row; i++){
			stateOfDFA[i][0] = new Integer(i).toString();//一般重命名从1开始
			if(stateN2D[i][0].state.contains("*")){
				stateOfDFA[i][0] += "*";
			}
		}
		
		//DFA其他部分在中间状态表添加*号之前完成。
		realRow = row;
		realCol = col;
	}
	public void outputN2DState(State[][] sta) throws IOException{
		int i, j;
		for(i = 0; !sta[i][0].state.isEmpty(); i++)
			;
		realRow = i;
		for(j = 0; !sta[0][j].state.isEmpty(); j++)
			;
		realCol = j;
//------------------------------控制台输出----------------------
//		for(i = 0; i < realRow; i++){
//			for(j = 0; j < realCol; j++){
//				if(i == 0 && j == 0){
//					System.out.print("I");
//				} else {
//					System.out.print(sta[i][j].state);
//				}
//				System.out.print("		");
//			}
//			System.out.println("");
//		}
//------------------------输出到文件MiddleN2D.txt------------------
		FileWriter fwN2D = new FileWriter("MiddleN2D.txt");
		for(i = 0; i < realRow; i++){
			for(j = 0; j < realCol; j++){
				if(i == 0 && j == 0){
					fwN2D.write("I");
				} else {
					fwN2D.write(sta[i][j].state.toString());
				}
				fwN2D.write("	");
			}
			fwN2D.write("\r\n");
		}
		fwN2D.close();
	}
	public void outputDFAState(String[][] sta) throws IOException{
		int i, j;
		for(i = 0; !(sta[i][0].isEmpty()); i++)
			;
		realRow = i;
		for(j = 0; !(sta[0][j].isEmpty()); j++)
			;
		realCol = j;
//		System.out.println("realRow = " + realRow + ",realCol = " + realCol);
//------------------------------控制台输出----------------------
//		for(i = 0; i < realRow; i++){
//			for(j = 0; j < realCol; j++){
//				if(i == 0 && j == 0){
//					System.out.print("");
//				} else {
//					System.out.print(sta[i][j]);
//				}
//				System.out.print("	");
//			}
//			System.out.println("");
//		}
//------------------------输出到文件GeneDFA.txt------------------
		FileWriter fwDFA = new FileWriter("GeneDFA.txt");
		for(i = 0; i < realRow; i++){
			for(j = 0; j < realCol; j++){
				if(i == 0 && j == 0){
					fwDFA.write("");
				} else {
					fwDFA.write(sta[i][j]);
				}
				fwDFA.write("	");
			}
			fwDFA.write("\r\n");
		}
		fwDFA.close();
	}
	
	public void outputTransState(State[][] n2dState, 
			String[][]nfaState) throws IOException{//输出中间状态表到最终结果的转换情况
	//只记录行数即可
		int i;
		for(i = 0; !(n2dState[i][0].state.isEmpty()); i++)
			;
		realRow = i;
		
		FileWriter fwTrans = new FileWriter("TransState.txt");
		fwTrans.write("从中间状态表到DFA，状态的转换关系：\r\n");
		for(i = 1; i < realRow; i++){
			fwTrans.write(n2dState[i][0].state.toString() + " -> " + 
		nfaState[i][0] + "\r\n");
		}
		fwTrans.close();
	}
	public static void main(String[] args) throws IOException{
		NFA2DFA nfa2dfa = new NFA2DFA();
		nfa2dfa.inputNFAState(nfa2dfa.stateOfNFA);
		nfa2dfa.transform();
		nfa2dfa.outputN2DState(nfa2dfa.stateN2D);
		nfa2dfa.outputDFAState(nfa2dfa.stateOfDFA);
		nfa2dfa.outputTransState(nfa2dfa.stateN2D, nfa2dfa.stateOfDFA);
		JOptionPane.showMessageDialog(null,
				"NFA已经成功确定化，生成文件在GeneDFA.txt中");
	}
}
