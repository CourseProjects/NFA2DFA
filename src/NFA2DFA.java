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
 * ʵ��NFA��DFA��ת��
 * �㷨���Ӽ���
 * ����NFA��״̬��ʹ��Set�洢
 * ����ʹ��LinkedHashSet������HashSet��
 * ��ΪLinkedHashSetԪ��ȡ�����ݺͲ�������һ�£�
 * �ɱ�֤����������������ͬ
 * ����ȻHashSet�Ľ���ȼۣ�����Щ��Ҫ�µĶ�Ӧ��ϵ��
 * 
 * ���������Ϊ״̬����
 * ����NFA״̬����stateOfNFA�ĸ�ʽΪ��
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
 * ����'e_null'��ʾ�մ�e,'s_null'��ʾ�ռ�
 * ��һ�б�ʾ��ĸ���е���ĸ����һ�б�ʾ״̬���е�״̬�����Ǽ��ϣ�
 * ��NFA����P37
 * 
 * NFAȷ�������̹����״̬��stateN2D�ĸ�ʽΪ��
 * I			Ia			Ib
 * 1,2			5,4,6,7,2	3,8
 * 5,4,6,7,2,*	s_null		3,9,8
 * 3,8,*		9			s_null
 * 3,9,8		9			s_null
 * 9,*			s_null		s_null
 * ����'s_null'��ʾ�ռ�
 * ��һ�б�ʾIaΪI��e�հ���a��e�հ�...����һ�б�ʾ�µ�״̬��
 * д���ļ�ʱs_null��ʾΪ[]
 * 
 * �����ļ�ΪNFA.txt����Ȼ������JOptionPane�����ɵ��������룬���ǡ�������˵�ɣ�
 * �����м�״̬���ļ�MiddleN2D.txt
 * ��������DFA�ļ�GeneDFA.txt
 * �����м�״̬���е�״̬��DFA״̬��ת�������ļ�TransState.txt
 * @author ��������ѧ	1120122018	��һ��
 *
 */
public class NFA2DFA {
	private final int MAX = 100;//�����100*100��״̬����
	private State[][] stateOfNFA = new State[MAX][MAX];//��Ҫ�ڹ��췽����new�����ڴ�
	private String[][] stateOfDFA = new String[MAX][MAX];
	private State[][] stateN2D = new State[MAX][MAX];//NFAȷ�������̹����״̬��
	private int row, col;//���м���
	private int realRow, realCol;//ʵ�������������
	
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
		/*���ڲ��ܶ��巺�����飬��������Set<String>[]�Ķ�����ʽ������һ���򵥵���State����ʾNFA��
		 * ״̬���Ӷ��ﵽSet<String>[]������Ŀ��
		 */
		Set<String> state = new LinkedHashSet<String>();
	}
	private Set<String> eee_Closure(Set<String> eSta){//e�հ��������eee...e=e�����
		Set<String> e_cls = new LinkedHashSet<String>();//һ��e�հ�״̬��
		Set<String> e_cls2nd = new LinkedHashSet<String>();//����e�հ�״̬��
//		int count = 1;//����
		e_cls = e_Closure(eSta);
		e_cls2nd = e_Closure(e_cls);//ͨ���ݹ���ã����eee...e=e�����ظ����e�����
		while(!e_cls2nd.equals(e_cls)){//�����ʱ����������e�հ�
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
	private Set<String> e_Closure(Set<String> eSta){//e�հ�����Ϊһ��״̬�������Ϊһ��״̬��
//		System.out.println("eSta = " + eSta);
		Set<String> e_cls = new LinkedHashSet<String>();//һ��e�հ�״̬��
		Set<String> fqeSet = new LinkedHashSet<String>();//f(q,e)����ļ���
		int i, j;//��¼f(q,e)�����NFA״̬���е�������
		int row, col;//����NFA״̬���ָ��
		Iterator<String> it = eSta.iterator();
		while(it.hasNext()){
			String q = it.next();
//			System.out.println("q = " + q);
			if(eSta.contains(q)){//��q��I����q��e_closure(I);
				e_cls.add(q);
			//--------------Ѱ��f(q,e)----------------
				i = j = 0;//��ʼ��i,j
				for(row = 0; row < realRow; row++){//Ѱ��q״̬���ڵ�����
					//Ҫע��״̬������ܻ���*�ţ������ж�ʱ��Ҫ�ж�q����q+"*"�Ƿ��ڼ�����
					if(stateOfNFA[row][0].state.contains(q)||stateOfNFA[row][0].state.contains(q+"*")){
						i = row;
						break;
					}
				}
				for(col = 0; col < realCol; col++){//Ѱ��e_null���ڵ�����
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
				}else {//��q��I���������q'����f(q,e),��q'����e_closure(I);
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
	private Set<String> Ia(Set<String> I, String a){//IaΪI��e�հ���a��e�հ�
		
		Set<String> eee_clsOfI = new LinkedHashSet<String>();//I��e�հ�
		Set<String> fqaSet = new LinkedHashSet<String>();//f(q,a)����ļ���
		Set<String> fqaSetUnion = new LinkedHashSet<String>();//����f(q,a)����ļ��ϵĲ���
		int i, j;//��¼f(q,e)�����NFA״̬���е�������
		int row, col;//����NFA״̬���ָ��
		
		eee_clsOfI = eee_Closure(I);
//		System.out.println("eee_clsOfI = " + eee_clsOfI);
	//------------��q����eee_closure(I)ʱ��f(q,a)����
		Iterator<String> it = eee_clsOfI.iterator();
		while(it.hasNext()){
			String q = it.next();//ȡ��eee_closure(I)�е�״̬q
			i = j = 0;//��ʼ��i,j
			for(row = 0; row < realRow; row++){//Ѱ��q״̬���ڵ�����
				//Ҫע��״̬������ܻ���*�ţ������ж�ʱ��Ҫ�ж�q����q+"*"�Ƿ��ڼ�����
				if(stateOfNFA[row][0].state.contains(q)||stateOfNFA[row][0].state.contains(q+"*")){
					i = row;
					break;
				}
			}
			for(col = 0; col < realCol; col++){//Ѱ��e_null���ڵ�����
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
			this.row = this.col = 0;	//��ʼ������
//			System.out.println(line);
			while(line != null){
				Scanner scanner1 = new Scanner(line);//���ո�ͻس�����ÿ������
				scanner1.useDelimiter("[ |\n]");
				while(scanner1.hasNext()){
					String s1 = scanner1.next();
//					System.out.println("scanner1: " + s1);
					Scanner scanner2 = new Scanner(s1);//����ÿ�������ڵ�Ԫ��
					scanner2.useDelimiter(",");
//					sta[row][col] = new State(); //����Ҫ����MAX���
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
		Set<Set<String>> Q_ = new LinkedHashSet<Set<String>>();//Q'(Q_)��stateN2D�ĵ�һ��ժ����ΪDFA��״̬��
		Set<String> M = new LinkedHashSet<String>();//MΪFA����ĸ��
		Set<String> tempSet = new LinkedHashSet<String>();//��ʱ���Ia��Ib�Ľ��
		int row, col;//colΪѰ��e_null����,row,col�����м�״̬��������������ɱ�
		boolean flag;//��־Q'���Ƿ���Ԫ�ص����
		String[] dealed = new String[MAX];//���洦�����״̬
		boolean deal;//dealΪfalse��ʾû�б������
		int count;//��¼�������״̬��
		int i, j;
//----------------------����״̬��Q'--------------------------------------	
		for(col = 1; col < realCol; col++){//����ĸ���stateOfNFA��ȡ��
			if(stateOfNFA[0][col].state.contains("e_null")){//��ĸ����ȥ��e_null
				;
			} else{
				M.addAll(stateOfNFA[0][col].state);
			}
		}
//		System.out.println("M = " + M);
		Q_.add(eee_Closure(stateOfNFA[1][0].state));//��ʼ��Q'Ϊe_closure{q0}
//		System.out.println("Q' init:" + Q_);	
		count = 0;
		Iterator<Set<String>> itQ_ = Q_.iterator();
		while(itQ_.hasNext()){//����״̬��X��Q'
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
				while(itM.hasNext()){//����A��M
					String a = itM.next();
//					System.out.println("a = " + a);
					tempSet = Ia(X, a);//�����״̬��X�������ַ�a�����Iaֵ
//					System.out.println("tempSet = " + tempSet);
					if(!tempSet.isEmpty()&&!Q_.contains(tempSet)){
						Q_.add(tempSet);
						flag = true;
					}
//					System.out.println("Q' = " + Q_);
				}
				if(flag){
					/*���Q'���¼����Ԫ�أ�����µ�����Ϊ�µ�Q'�������©���¼����Ԫ�ء�
					 * ͨ����dealed������жϵõ�״̬�Ƿ��������Ϣ���Ӷ��ظ�����
					 */
					itQ_ = Q_.iterator();
				}
				dealed[count++] = X.toString();//�������״̬����dealed��
			}
		}
//		System.out.println("Q': " + Q_);
		
//-----------------------------�����м����״̬��stateN2D-----------------------
		//stateN2D����ΪrealCol-1,����e_null��������0���ϼӣ�ֱ��Q'����Ԫ�ء�
		col = 1;
		stateN2D[0][0].state.add("I");//���Ͻ�ΪI
		String[] rowNo1 = new String[MAX];//�м�״̬��ĵ�һ�е���ĸ
		Iterator<String> itM = M.iterator();//������ĸ��,M�в���e_null
		//(�������������ʱ������itM��������������)
		while(itM.hasNext()){
			String itMS = itM.next();
			stateN2D[0][col].state.add(itMS);//��һ��ΪIa,Ib
//			System.out.println("stateN2D[0][col].state = " + stateN2D[0][col].state);
			rowNo1[col] = itMS;
			stateOfDFA[0][col] = itMS;
			col++;
		}
		row = 1;
		itQ_ = Q_.iterator();//����״̬��
		Set<String> X = null;
		while(itQ_.hasNext()){
			X = itQ_.next();
			stateN2D[row][0].state.addAll(X);
			for(j = 1; j < col; j++){//������״̬����������Ϣ
//				System.out.println("X = " + X);
				stateN2D[row][j].state.addAll(Ia(X, rowNo1[j]));
				//toString���������ص��ַ�������[]��������
//				System.out.println("row = " + row + ",j = " + j);
//				System.out.println("stateN2D[row][j].state = " + stateN2D[row][j].state);
			}
			row++;
		}
//-----------------------------����DFA���м䲿������--------------------
		//�����м�״̬��Ѱ�����±�Ŷ�Ӧ��״̬��������ӳ�䵽DFA���С�
		for(i = 1; i < row; i++){
			for(j = 1; j < col; j++){
				for(int k = 1; k < row; k++){
					if(stateN2D[i][j].state.isEmpty()){//�м�״̬��ֵΪ�գ�����DFA�����'/'
						stateOfDFA[i][j] = "/";
					}
					if(stateN2D[i][j].state.equals(stateN2D[k][0].state)){
						stateOfDFA[i][j] = new Integer(k).toString();
//						System.out.println(stateOfDFA[i][j]);
					}
				}
			}
		}
		
//----------------------------����ս����*---------------------------------
//		System.out.println("ending *");
	//�Ƚ�ԭNFA�е��ս�״̬�ҳ�����ȥ��*�ţ��������м�״̬���е��Ǹ�״̬���У�Ȼ�������м���*��
		//�ҳ�NFA�е��ս�״̬
		Iterator<String> it = null;
		String q = null;
		String[] endSta = new String[MAX];//��¼�ս�״̬��ȥ*�ŵ��ַ���
//		int count;//endSta�ļ�����
		for(i = 1, count = 0; i < realRow; i++){
			it = stateOfNFA[i][0].state.iterator();
			while(it.hasNext()){
				q = it.next();
				if(q.endsWith("*")){//��*�ŵ�״̬����*�Ž�β
					endSta[count++] = q.substring(0, q.length()-1);
				}
			}
		}
		
		//�����м�״̬���е�״̬�������*��
//		System.out.println("row = " + row);
		for(i = 1; i < row; i++){
			X = stateN2D[i][0].state;
			for(j = 0; j < endSta.length; j++){
				if(X.contains(endSta[j])&&!X.contains("*")){//�����ս����û��*��
					stateN2D[i][0].state.add("*");
				}
			}
//			System.out.println("stateN2D[i][0].state = " + stateN2D[i][0].state);
		}
		
//-------------------------�������յ�DFA��----------------------------
	//DFA��������м�״̬�����������ͬ����Ϊ1��2��3...,������ս�״̬������*��
		//ΪDFA�ĵ�һ�е�һ�и�ֵ
		stateOfDFA[0][0] = "head";
		//DFA��һ���ڽ�a,b...װ���м�״̬��ʱ�Ѿ�װ��
		
		//DFA��һ��Ϊ1��2��3...,������ս�״̬������*��
		for(i = 1; i < row; i++){
			stateOfDFA[i][0] = new Integer(i).toString();//һ����������1��ʼ
			if(stateN2D[i][0].state.contains("*")){
				stateOfDFA[i][0] += "*";
			}
		}
		
		//DFA�����������м�״̬�����*��֮ǰ��ɡ�
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
//------------------------------����̨���----------------------
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
//------------------------������ļ�MiddleN2D.txt------------------
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
//------------------------------����̨���----------------------
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
//------------------------������ļ�GeneDFA.txt------------------
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
			String[][]nfaState) throws IOException{//����м�״̬�����ս����ת�����
	//ֻ��¼��������
		int i;
		for(i = 0; !(n2dState[i][0].state.isEmpty()); i++)
			;
		realRow = i;
		
		FileWriter fwTrans = new FileWriter("TransState.txt");
		fwTrans.write("���м�״̬��DFA��״̬��ת����ϵ��\r\n");
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
				"NFA�Ѿ��ɹ�ȷ�����������ļ���GeneDFA.txt��");
	}
}
