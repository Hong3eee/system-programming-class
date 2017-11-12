import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.StringTokenizer;

public class SICAssembler {
	final int MAX_INST = 256;
	final int MAX_LINES = 5000;
	final int MAX_OPERAND = 3;
	
	final int N = 32;
	final int I = 16;
	final int X = 8;
	final int B = 4;
	final int P = 2;
	final int E = 1;
	//////////////////////////////////////////////
	inst_unit inst_table[] = new inst_unit[MAX_INST];
	int inst_index = 0;
	
	String input_data[] = new String[MAX_LINES];
	int line_num;
	int label_num;
	
	token_unit token_table[] = new token_unit[MAX_LINES];
	int token_line = 0;
	
	symbol_unit sym_table[] = new symbol_unit[MAX_LINES];
	literal_unit lit_table[] = new literal_unit[100];
	modification_unit mod_table[] = new modification_unit[100];
		
	int hasliteral[] = new int[10];
	int modification_index[] = new int[100];
	int modification_line = 0;
	int symbol_line = -1;
	int literal_line = -1;
	int section_num = 0;
	int locctr;
	int section_length[] = new int[10];
	
	String input_file;
	String output_file;
	////////////////////////////////////////
	public SICAssembler()
	{
		for(int i = 0 ; i<100;i++)							//modification unit �ν��Ͻ� �Ҵ�
			mod_table[i] = new modification_unit();
		
		if(init_my_assembler() < 0 )
			System.out.println("Error Occured in init_my_assembler");
		if(assem_pass1() < 0 )
			System.out.println("Error Occured in pass1");
		if(assem_pass2() < 0 )
			System.out.println("Error Occured in pass2");

		make_objectcode_output("output_139.txt");
	}
	/* ----------------------------------------------------------------------------------
	* ���� : ���α׷� �ʱ�ȭ�� ���� �ڷᱸ�� ���� �� ������ �д� �Լ��̴�.
	* �Ű� : ����
	* ��ȯ : �������� = 0 , ���� �߻� = -1
	* ���� : ������ ��ɾ� ���̺��� ���ο� �������� �ʰ� ������ �����ϰ� �ϱ�
	*		   ���ؼ� ���� ������ �����Ͽ� ���α׷� �ʱ�ȭ�� ���� ������ �о� �� �� �ֵ���
	*		   �����Ͽ���.
	* ----------------------------------------------------------------------------------
	*/
	int init_my_assembler()
	{
		int result=0;
		if ((result = init_inst_file("inst.data")) < 0)
			return -1;
		if ((result = init_input_file("input.txt")) < 0)
			return -1;
		return result;
	}
	
	/* ----------------------------------------------------------------------------------
	* ���� : �ӽ��� ���� ��� �ڵ��� ������ �о� ���� ��� ���̺�(inst_table)��
	*        �����ϴ� �Լ��̴�.
	* �Ű� : ���� ��� ����
	* ��ȯ : �������� = 0 , ���� < 0
	* ���� : ���� ������� ������ �����Ӱ� �����Ѵ�. ���ô� ������ ����.
	*
	*	===============================================================================
	*		   | �̸� | ���� | ���� �ڵ� | ���۷����� ���� | NULL|
	*	===============================================================================
	*
	* ----------------------------------------------------------------------------------
	*/
	int init_inst_file(String inst_file)
	{
		int errno = 0;
		int i = 0;

		try
		{
			Scanner scanner = new Scanner(new File(inst_file));						
			
			while(scanner.hasNext()){	
				StringTokenizer st = new StringTokenizer(scanner.nextLine(),"\t\n");		//��ĳ�� Ŭ������ �̿��� ���پ� �޾ƿ� + ��,�������� ��ũ������
				
				inst_table[i] = new inst_unit();

				inst_table[i].operator = st.nextToken();
				inst_table[i].type = st.nextToken();
				inst_table[i].opcode = st.nextToken();
				inst_table[i].operandAmount = Integer.parseInt(st.nextToken());
				
				inst_index = i;			//0���� ���� ����

				i++;
			}
			scanner.close();
		}
		catch (FileNotFoundException e) {				//������ ������ try catch �� ����ó��
			System.out.println("The inst_file doesn't exist");
			e.printStackTrace();
			return errno = -1;
		}
		return errno;
	}
	
	/* ----------------------------------------------------------------------------------
	* ���� : ����� �� �ҽ��ڵ带 �о���� �Լ��̴�. �о���鼭 �ٷ�  token �� �и��Ͽ� ���̺� �־��ش�.
	* �Ű� : ������� �ҽ����ϸ�
	* ��ȯ : �������� = 0 , ���� < 0
	* ���� :
	*
	* ----------------------------------------------------------------------------------
	*/
	int init_input_file(String input_file)
	{
		int errno = 0;
		int i = 0;

		try
		{
			Scanner scanner = new Scanner(new File(input_file));

			while(scanner.hasNext()){
				String line = scanner.nextLine();
				char first = line.charAt(0);

				StringTokenizer st = new StringTokenizer(line,"\t\n");
				line_num = i;
				
				token_table[i] = new token_unit();
				token_table[i].label ="";
				token_table[i].operator ="";
				token_table[i].operand[0] ="     ";
				token_table[i].comment ="";

				if(first == '\t')
					token_table[i].label ="";
				else
					token_table[i].label =st.nextToken();
				
				if(st.hasMoreTokens())
					token_table[i].operator =st.nextToken();
				
				if(st.hasMoreTokens())
					token_table[i].operand[0] =st.nextToken();
				
				token_table[i].total_operand = token_table[i].operand[0];
				token_table[i].operand[1] = "     ";
				token_table[i].operand[2] = "     ";
				
				StringTokenizer st2 = new StringTokenizer(token_table[i].operand[0],",\t");
				for(int j=0;j<3;j++)
				{
					if(st2.hasMoreTokens())
						token_table[i].operand[j] = st2.nextToken();
				}
				
				if(st.hasMoreTokens())
					token_table[i].comment =st.nextToken();
				
			
				token_line = i;	//0���� ����
				i++;
			}
			scanner.close();
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("The input_file doesn't exist");
			e.printStackTrace();
			return errno = -1;
		}
		return errno;
	}

	/* ----------------------------------------------------------------------------------
	* ���� : �Է� ���ڿ��� ���� �ڵ������� �˻��ϴ� �Լ��̴�.
	* �Ű� : ��ū ������ ���е� ���ڿ�
	* ��ȯ : �������� = ���� ���̺� �ε���, ���� < 0
	* ���� :
	*
	* ----------------------------------------------------------------------------------
	*/
	int search_opcode(String str)	//str�� �񱳰����� �ؼ� inst_table�� ��� ������ index�� ã��. ������ -1 ����
	{
		String compare;

		if (str.charAt(0) == '+')					//4���� ó�� +�� ����
			compare = str.substring(1);
		else
			compare = str;


		for (int i = 0; i <= inst_index; i++)
		{
			if (compare.equals(inst_table[i].operator))
				return i;
		}

		return -1;
	}

	/* ----------------------------------------------------------------------------------
	* ���� : �ɺ����� ���Ͽ� ������ ���̺� �߰��ϰ� ������ �����߻�
	* �Ű� : ���� symbol
	* ��ȯ : symbol�� ��� �߰��� �� ���� = 0, �̹� symbol�� �����ϸ� -1
	* -----------------------------------------------------------------------------------
	*/
	int manage_symbol(String str)
	{
		for (int i = 0; i <= symbol_line; i++)
		{
			if ((str.equals(sym_table[i].symbol)) && section_num == sym_table[i].section)	//symbol�� �����鼭 �� symbol�� ���������϶� 
				return -1;
		}
		
		//symbol�� ������ symtab �� �߰�, line �� ����
		sym_table[symbol_line + 1] = new symbol_unit();		//C�� �޸� �ν��Ͻ��� ��������� �ϹǷ� ����
		sym_table[symbol_line + 1].symbol= str;
		sym_table[symbol_line + 1].addr = locctr;
		sym_table[symbol_line + 1].section = section_num;
		sym_table[symbol_line + 1].sign = '+';
		symbol_line++;
		return 0;

	}

	/* ----------------------------------------------------------------------------------
	* ���� : ���ͷ����� ���Ͽ� ������ ���̺� �߰��ϰ� ������ �����߻�
	* �Ű� : ���� literal
	* ��ȯ : literal�� ��� �߰��� �� ���� = 0, �̹� literal�� �����ϸ� -1
	* -----------------------------------------------------------------------------------
	*/
	int manage_literal(String str)
	{
		for (int i = 0; i <= literal_line; i++)
		{
			if (str.equals(lit_table[i].literal))// && section_num == lit_table[i].section)	//literal�� �����鼭 �� literal�� ���������϶� 
				return -1;
		}

		//literal�� ������ littab �� �߰�, line �� ����
		lit_table[literal_line+1] = new literal_unit();		//C�� �޸� �ν��Ͻ��� ��������� �ϹǷ� ����
		lit_table[literal_line + 1].literal =  str;
		lit_table[literal_line + 1].addr = locctr;
		lit_table[literal_line + 1].object = 0;
		lit_table[literal_line + 1].section = section_num;
		hasliteral[section_num] = 1;					// �� ���ǿ��� ���ͷ��� ���̴� �� ������ �Ǻ��� ������ �ִ� �迭 1-> ���ͷ��� ���ǿ� ����, 2-> ���ǿ� ����
		literal_line++;
		return 0;

	}

	/* ----------------------------------------------------------------------------------
	* ���� : �ɺ����� ���Ͽ� ������ �ּ� ��ȯ, �ٸ� ��� ���� ���� ��ȯ
	* �Ű� : ���� ���ڿ�
	* ��ȯ : symbol�� �����ϰ�, �������ǿ� ���� �� = �ɺ��� �����Ǵ� �ּҰ� ��ȯ,
			 symbol�� �����ϳ� �ٸ� ���ǿ� ������ = -2 ��ȯ,
			 symbol�� ���� �����ʴٸ� -1�� ���� ��ȯ
	* -----------------------------------------------------------------------------------
	*/
	int search_symbol(String str)
	{
		int addr = -1;
		for (int i = 0; i <= symbol_line; i++)
		{
			if (str.equals(sym_table[i].symbol))
			{
				if (section_num == sym_table[i].section)	//symbol�� �����鼭 �� symbol�� ���������϶� 
					return sym_table[i].addr;
				else										//symbol�� ������ ���� ������ �ƴҶ��� -2 ��ȯ
					addr = -2;
			}
		}

		return addr;
	}

	/* ----------------------------------------------------------------------------------
	* ���� : ���ڿ��� ���ͷ� ���̺��� ���� ��
	* �Ű� : ���� ���ڿ�
	* ��ȯ : literal�� ������ = �� ���ͷ��� �ּҰ� ��ȯ,
			 �̹� literal�� �������� ������ -1�� ���� ��ȯ
	* -----------------------------------------------------------------------------------
	*/
	int search_literal(String str)
	{
		for (int i = 0; i <= literal_line; i++)
		{
			if (str.equals(lit_table[i].literal))
				return lit_table[i].addr;
		}

		return -1;
	}

	/* ----------------------------------------------------------------------------------
	* ���� : �Է� ���ڿ��� ���ͷ� �ε����� ã�� �Լ��̴�.
	* �Ű� : ���� ���ڿ�
	* ��ȯ : �������� = ���ͷ� ���̺� �ε���, ���� < 0
	* ----------------------------------------------------------------------------------
	*/
	int search_literal_index(String str)	//str�� �񱳰����� �ؼ� inst_table�� ��� ������ index�� ã��. ������ -1 ����
	{
		for (int i = 0; i <= literal_line; i++)
		{
			if (str.equals(lit_table[i].literal))
				return i;
		}

		return -1;
	}

	/* ----------------------------------------------------------------------------------
	* ���� : �Է� ���ڿ��� �ɺ� �ε����� ã�� �Լ��̴�.
	* �Ű� : ���� ���ڿ�
	* ��ȯ : �������� = �ɺ� ���̺� �ε���, ���� < 0
	* ----------------------------------------------------------------------------------
	*/
	int search_symbol_index(String str)	//str�� �񱳰����� �ؼ� inst_table�� ��� ������ index�� ã��. ������ -1 ����
	{
		for (int i = 0; i <= symbol_line; i++)
		{
			if (str.equals(sym_table[i].symbol))
				return i;
		}

		return -1;
	}
	
	
	
	/* ----------------------------------------------------------------------------------
	* ���� : ����� �ڵ带 ���� �н�1������ �����ϴ� �Լ��̴�.
	*		   �н�1������..
	*		   1. ���α׷� �ҽ��� ��ĵ�Ͽ� �ش��ϴ� ��ū������ �и��Ͽ� ���α׷� ���κ� ��ū
	*		   ���̺��� �����Ѵ�.
	*
	* �Ű� : ����
	* ��ȯ : ���� ���� = 0 , ���� = < 0
	* ���� : ���� �ʱ� ���������� ������ ���� �˻縦 ���� �ʰ� �Ѿ �����̴�.
	*	  ���� ������ ���� �˻� ��ƾ�� �߰��ؾ� �Ѵ�.
	*
	* -----------------------------------------------------------------------------------
	*/
	int assem_pass1()
	{
		
		int index = 0;
		int ltorg_line = 0;
		for (int i = 0; i <= line_num; i++)
		{

			if (token_table[i].operator.equals("START"))		//START �� LOCCTR �� �� ���ڷ� �ʱ�ȭ
				{
				locctr = Integer.valueOf(token_table[i].operand[0],16);
				}
			else if (token_table[i].label.equals( "."))		//�ּ� ó��, �ּ��� opcode�� ã�� �ʿ䰡 ����.
				continue;


			token_table[i].current_locctr = locctr;		//�� ��ū�� locctr ���

			if (token_table[i].operator.equals("CSECT"))	//Control section�� �ٲ� -> locctr �ʱ�ȭ, section_num���� �迭 ������ ����
			{
				section_length[section_num] = locctr;			//�� ������ ���� �����ص�
				locctr = 0;
				section_num++;
				continue;
			}

			if (!token_table[i].label.equals("")) // ���� ������	�ɺ� ó��
				if (manage_symbol(token_table[i].label) < 0)
					return -1;										//���� ���� ���� ���� �̸��� ���� �����ϸ� ���� ���

			if ((index = search_opcode(token_table[i].operator)) > -1)		//opcode�� ������, �̶��� pc�� ���, index�� ���̺� �����Ѵ�
			{
				token_table[i].hasobjectcode = 1;							//object�ڵ带 �����ϴ� ��ū���� ���
				if (inst_table[index].type.equals("1"))	// format 1 �� ��
				{
					locctr = locctr + 1;
					token_table[i].pcValue = locctr;

				}
				else if (inst_table[index].type.equals("2")) // format 2 �� ��
				{
					locctr = locctr + 2;
					token_table[i].pcValue = locctr;
				}
				else if (inst_table[index].type.equals("3/4")) // format 3/4 �� �� +���η� locctr �����Ǵ� �� ������
				{
					if (token_table[i].operator.charAt(0) == '+')
						locctr = locctr + 4;
					else
						locctr = locctr + 3;

					token_table[i].pcValue = locctr;
				}
			}
			else						// opcode�� ���� directive �� ��
			{
				if (token_table[i].operator.equals("WORD"))
				{
					token_table[i].hasobjectcode = 1;
					locctr = locctr + 3;
				}
				else if (token_table[i].operator.equals("RESW"))
				{
					locctr = locctr + 3 * Integer.parseInt(token_table[i].total_operand);
				}
				else if (token_table[i].operator.equals("RESB"))
				{
					locctr = locctr + Integer.parseInt(token_table[i].total_operand);
				}
				else if (token_table[i].operator.equals("BYTE"))	//ù ���ڰ� X(16����, ���� 2���� 1����Ʈ), C(���� 1���� 1����Ʈ) �϶� ó��
				{
					token_table[i].hasobjectcode = 1;

					StringTokenizer st = new StringTokenizer(token_table[i].total_operand,"'");
					String tok = st.nextToken();
					
					if (tok.equals("C"))
					{
						tok = st.nextToken();
						locctr = locctr + tok.length();
					}
					else if (tok.equals("X"))
					{
						tok = st.nextToken();
						locctr = locctr + tok.length()/2;
					}
				}
				else if (token_table[i].operator.equals("LTORG"))
				{
					
					for (int j = ltorg_line; j < i; j++)
					{
						if (token_table[j].operand[0].charAt(0) == '=')
						{
							if (manage_literal(token_table[j].operand[0]) < 0)		//�̹� �ִ� ���ͷ��� ����
								continue;

							StringTokenizer st = new StringTokenizer(token_table[j].operand[0],"'");
							String tok = st.nextToken();

							if (tok.equals("=C"))
							{
								tok = st.nextToken();
								locctr = locctr + tok.length();
							}
							else if (tok.equals("=X"))
							{
								tok = st.nextToken();
								locctr = locctr + tok.length()/2;
							}
						}
					}

					ltorg_line = i;					//ltorg ���� ����. ���� ltorg���� ���� ���ͷ��� �����ϵ��� �ؾ��ϱ⶧��
				}
				else if (token_table[i].operator.equals("END"))
				{
					for (int j = ltorg_line; j < i; j++)
					{
						if (token_table[j].operand[0].charAt(0) == '=')
						{
							if (manage_literal(token_table[j].operand[0]) < 0)		//�̹� �ִ� ���ͷ��� ����
								continue;

							StringTokenizer st = new StringTokenizer(token_table[j].operand[0],"'");
							String tok = st.nextToken();

							if (tok.equals("=C"))
							{
								tok = st.nextToken();
								locctr = locctr + tok.length();
							}
							else if (tok.equals("=X"))
							{
								tok = st.nextToken();
								locctr = locctr + tok.length()/2;
							}
						}
					}
					section_length[section_num] = locctr;

					ltorg_line = i;					//ltorg ���� ����. ���� ltorg���� ���� ���ͷ��� �����ϵ��� �ؾ��ϱ⶧��
				}
			}
		}
		
		return 0;
	}
	
	/* ----------------------------------------------------------------------------------
	* ���� : ����� �ڵ带 ���� �ڵ�� �ٲٱ� ���� �н�2 ������ �����ϴ� �Լ��̴�.
	*		   �н� 2������ ���α׷��� ����� �ٲٴ� �۾��� ���� ������ ����ȴ�.
	*		   ������ ���� �۾��� ����Ǿ� ����.
	*		   1. ������ �ش� ����� ��ɾ ����� �ٲٴ� �۾��� �����Ѵ�.
	* �Ű� : ����
	* ��ȯ : �������� = 0, �����߻� = < 0
	* ���� :
	* -----------------------------------------------------------------------------------
	*/
	int assem_pass2()
	{
		int index = 0;
		section_num = 0;
		int ltorg_line = 0;

		for (int i = 0; i <= line_num; i++)
		{
			if (token_table[i].operator.equals("START"))		//START �� LOCCTR �� �� ���ڷ� �ʱ�ȭ
			{
				locctr = Integer.valueOf(token_table[i].operand[0],16);

				System.out.printf("%04X	%s	%s	%s\n", locctr, token_table[i].label, token_table[i].operator,token_table[i].total_operand);
				continue;
			}
			else if (token_table[i].label.equals("."))		//�ּ� ó��, �ּ��� opcode�� ã�� �ʿ䰡 ����.
			{
				System.out.printf("%s	%s	%s	%s\n", token_table[i].label, token_table[i].operator,token_table[i].total_operand, token_table[i].comment);

				continue;
			}


			if (token_table[i].operator.equals("CSECT"))	//Control section�� . -> locctr �ʱ�ȭ, section_num���� �迭 ������ ����
			{
				locctr = 0;
				section_num++;
			}

			if ((index = search_opcode(token_table[i].operator)) > -1)		//opcode�� ������, �̶��� pc�� ���
			{
				if (inst_table[index].type.equals("1"))	// format 1 �� ��
				{
					token_table[i].object = Integer.valueOf(inst_table[index].opcode,16); 

					System.out.printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i].label, token_table[i].operator,token_table[i].total_operand, token_table[i].object);
					locctr = locctr + 1;
				}
				else if (inst_table[index].type.equals("2")) // format 2 �� �� operand�� ���� �������Ϳ� ���� ����
				{
					token_table[i].object = Integer.valueOf(inst_table[index].opcode,16); 
					
					for (int j = 0; j < 2; j++)
					{
						token_table[i].object = token_table[i].object << 4;			//�ּҰ��. opcode�� �� �� 4��Ʈ�� �и鼭 operand�� ���

						if (token_table[i].operand[j].equals(""))
							token_table[i].object += 0;
						else
						{
							if (token_table[i].operand[j].equals("A"))
								token_table[i].object += 0;
							else if (token_table[i].operand[j].equals("X"))
								token_table[i].object += 1;
							else if (token_table[i].operand[j].equals("L"))
								token_table[i].object += 2;
							else if (token_table[i].operand[j].equals("B"))
								token_table[i].object += 3;
							else if (token_table[i].operand[j].equals("S"))
								token_table[i].object += 4;
							else if (token_table[i].operand[j].equals("T"))
								token_table[i].object += 5;
							else if (token_table[i].operand[j].equals("F"))
								token_table[i].object += 6;
							else if (token_table[i].operand[j].equals("PC"))
								token_table[i].object += 8;
							else if (token_table[i].operand[j].equals("SW"))
								token_table[i].object += 9;
						}
					}

					System.out.printf("%04X	%s	%s	%s	%04X\n", locctr, token_table[i].label, token_table[i].operator,token_table[i].total_operand, token_table[i].object);
					locctr = locctr + 2;
				}
				else if (inst_table[index].type.equals("3/4"))
				{
					int target_addr = -1;
					int dest = 0;

					if (token_table[i].operator.charAt(0) == '+')				//4����
					{
						token_table[i].nixbpe += N + I + E;					//NIXBPE�߿� 4������ NIE �⺻, X�� �߰��ɼ� �����Ƿ� �� �Ҵ�
						if (token_table[i].operand[1].equals("X"))
							token_table[i].nixbpe += X;

						target_addr = search_symbol(token_table[i].operand[0]);
						if (target_addr == -2)							//���� ������ �ٸ� ���ǿ� ����. M���ڵ忡 ����ϹǷ� ����ü�� ���
						{
							mod_table[modification_line].index = i;
							mod_table[modification_line].section = section_num;
							modification_line++;
							target_addr = 0;
						}

						token_table[i].object = Integer.valueOf(inst_table[index].opcode,16) + (token_table[i].nixbpe >> 4);		//nixpbe�� ���� 2��Ʈ�� ���
						token_table[i].object = (token_table[i].object << 4) + (token_table[i].nixbpe & 15);						//15�� & ���� .���� 4��Ʈ �� ���
						token_table[i].object = (token_table[i].object << 20) + target_addr;										//���� (16������)3�ڸ� ����� 5�ڸ� �ڷ� �� ���� Ÿ���ּ� ���

						System.out.printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i].label, token_table[i].operator,token_table[i].total_operand, token_table[i].object);

						locctr = locctr + 4;
					}
					else												//3����
					{
						if (token_table[i].operand[0].charAt(0) == '#')					//immediate	addressing
						{
							String temp = token_table[i].operand[0].substring(1);
							target_addr = Integer.valueOf(temp,10);					//immediate�� 10���� �̹Ƿ�

							token_table[i].nixbpe += I;							// I  ��Ʈ�� ���

							token_table[i].object = Integer.valueOf(inst_table[index].opcode,16) + (token_table[i].nixbpe >> 4);		//nixpbe�� ���� 2��Ʈ�� ���
							token_table[i].object = (token_table[i].object << 4) + (token_table[i].nixbpe & 15);						//15�� & ���� .���� 4��Ʈ �� ���
							token_table[i].object = (token_table[i].object << 12) + target_addr;										//���� (16������)3�ڸ� ����� 3�ڸ� �ڷ� �� ���� Ÿ���ּ� ���

							System.out.printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i].label, token_table[i].operator,token_table[i].total_operand, token_table[i].object);

							locctr = locctr + 3;						
							continue;
						}
						else if (token_table[i].operand[0].charAt(0) == '@')								//indirect addressing
						{
							String temp = token_table[i].operand[0].substring(1);
							target_addr = search_symbol(temp);
							dest = target_addr - token_table[i].pcValue;							//target addr - pc ��

							token_table[i].nixbpe += N + P;

							token_table[i].object = Integer.valueOf(inst_table[index].opcode,16) + (token_table[i].nixbpe >> 4);		//nixpbe�� ���� 2��Ʈ�� ���
							token_table[i].object = (token_table[i].object << 4) + (token_table[i].nixbpe & 15);						//15�� & ���� .���� 4��Ʈ �� ���
							token_table[i].object = (token_table[i].object << 12) + dest;												//���� (16������)3�ڸ� ����� 3�ڸ� �ڷ� �� ���� dest �ּ� ���

							System.out.printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i].label, token_table[i].operator,token_table[i].total_operand, token_table[i].object);

							locctr = locctr + 3;
						}
						else																		//direct addressing
						{
							if (token_table[i].operand[0].charAt(0) == '=')						//���ͷ�
							{
								target_addr = search_literal(token_table[i].operand[0]);
							}
							else
							{
								if (inst_table[index].operandAmount == 0)					//operand�� ���� ��
								{
									target_addr = 0;										//RSUB ���� operand�� ���� ������ �ּҸ� 0���� ����

									token_table[i].nixbpe += N + I;

									token_table[i].object = Integer.valueOf(inst_table[index].opcode,16) + (token_table[i].nixbpe >> 4);
									token_table[i].object = (token_table[i].object << 4) + (token_table[i].nixbpe & 15);		//15�� & ���� .���� 4��Ʈ �� ���
									token_table[i].object = (token_table[i].object << 12) + target_addr;
									//System.out.println(""+locctr+"	"+token_table[i].label + "	" + token_table[i].operator + "	"+token_table[i].total_operand + "	"+token_table[i].object);

									System.out.printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i].label, token_table[i].operator,token_table[i].total_operand, token_table[i].object);

									locctr = locctr + 3;
									continue;
								}
								else														//operand�� ���� ��
									target_addr = search_symbol(token_table[i].operand[0]);
							}


							if (target_addr == -1)	//���� �߻� symbol�� �����µ� ����.
								return -1;
							else	//Ÿ�� �ּҸ� �޾ƿ�.
							{
								dest = target_addr - token_table[i].pcValue;
								if (-2048 <= dest && dest <= 2047)			//PC relative ��� ���� ���� �����ؾ���
								{
									token_table[i].nixbpe += N + I + P;

									token_table[i].object = Integer.valueOf(inst_table[index].opcode,16) + (token_table[i].nixbpe >> 4);
									token_table[i].object = (token_table[i].object << 4) + (token_table[i].nixbpe & 15);		//15�� & ���� ->���� 4��Ʈ �� ���
									if (dest < 0)
									{
										token_table[i].object = (token_table[i].object << 12) + (dest & 0x00000FFF);		//������ ��� ���� 3����Ʈ�� �ּҷ� ����ؾ��ϹǷ� ��Ʈ������
									}
									else
										token_table[i].object = (token_table[i].object << 12) + dest;

									System.out.printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i].label, token_table[i].operator,token_table[i].total_operand, token_table[i].object);

									locctr = locctr + 3;
								}
								else //error 
									return -1;
							}
						}
					}
				}
			}
			else						// opcode�� ���� directive �� ��
			{
				if (token_table[i].operator.equals("WORD"))
				{
					int check = 0;
					for (int j = 0; j < token_table[i].operand[0].length(); j++)
					{
						if (token_table[i].operand[0].charAt(j) == '-')						//- �� ������,
						{
							StringTokenizer st = new StringTokenizer(token_table[i].operand[0],"-\n");

							String tok = st.nextToken();
							int sym_index = search_symbol_index(tok);
							tok = st.nextToken();
							int sym_index2 = search_symbol_index(tok);
							sym_table[sym_index2].sign = '-';

							if (sym_table[sym_index].section != section_num || sym_table[sym_index2].section != section_num)
							{
								mod_table[modification_line].index = i;
								mod_table[modification_line].section = section_num;
								modification_line++;

								token_table[i].object = 0;
								System.out.printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i].label, token_table[i].operator,token_table[i].total_operand, token_table[i].object);

								locctr = locctr + 3;
								check = 1;
								break;
							}
						}
						else if (token_table[i].operand[0].charAt(j) == '+')					// + �� ������
						{
							StringTokenizer st = new StringTokenizer(token_table[i].operand[0],"+\n");

							String tok = st.nextToken();
							int sym_index = search_symbol_index(tok);
							tok = st.nextToken();
							int sym_index2 = search_symbol_index(tok);

							if (sym_table[sym_index].section != section_num || sym_table[sym_index2].section != section_num)
							{
								mod_table[modification_line].index = i;
								mod_table[modification_line].section = section_num;
								modification_line++;

								token_table[i].object = 0;
								System.out.printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i].label, token_table[i].operator,token_table[i].total_operand, token_table[i].object);

								locctr = locctr + 3;
								check = 1;
								break;
							}
						}
					}
					if (check == 0)														// �� ��ȣ�� �������� ������, �� ������� �ʾƵ��ɶ�
					{
						token_table[i].object = 3 * Integer.parseInt(token_table[i].operand[0]);
						System.out.printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i].label, token_table[i].operator,token_table[i].total_operand, token_table[i].object);

						locctr = locctr + 3;
					}
				}
				else if (token_table[i].operator.equals("BYTE"))	//ù ���ڰ� X(16����, ���� 2���� 1����Ʈ), C(���� 1���� 1����Ʈ) �϶� ó��
				{
					StringTokenizer st = new StringTokenizer(token_table[i].total_operand,"'");
					String tok = st.nextToken();
					
					if (tok.equals("C"))
					{
						tok = st.nextToken();
						for (int j = 0; j <tok.length(); j++)
						{
							token_table[i].object += tok.charAt(j);
							if (j == tok.length() - 1)
								continue;
							token_table[i].object = token_table[i].object << 8;					//����Ʈ �� ��ŭ object �ڵ� ����Ʈ��
						}
						System.out.printf("%04X	%s	%s	%s	%X\n", locctr, token_table[i].label, token_table[i].operator,token_table[i].total_operand, token_table[i].object);
						locctr = locctr + tok.length();
					}
					else if (tok.equals("X"))
					{
						tok = st.nextToken();
						token_table[i].object = Integer.valueOf(tok,16);
						System.out.printf("%04X	%s	%s	%s	%02X\n", locctr, token_table[i].label, token_table[i].operator,token_table[i].total_operand, token_table[i].object);
						locctr = locctr + tok.length() / 2;
					}
				}
				else if (token_table[i].operator.equals("RESW"))
				{
					System.out.printf("%04X	%s	%s	%s\n", locctr, token_table[i].label, token_table[i].operator,token_table[i].total_operand);
					locctr = locctr + 3 * Integer.parseInt(token_table[i].total_operand);
				}
				else if (token_table[i].operator.equals("RESB"))
				{
					System.out.printf("%04X	%s	%s	%s\n", locctr, token_table[i].label, token_table[i].operator,token_table[i].total_operand);
					locctr = locctr + Integer.parseInt(token_table[i].total_operand);
				}
				else if (token_table[i].operator.equals("LTORG") || token_table[i].operator.equals("END"))
				{
					System.out.printf("	%s	%s	%s\n", token_table[i].label, token_table[i].operator,token_table[i].total_operand);
					for (int j = ltorg_line; j < i; j++)
					{
						if (token_table[j].operand[0].charAt(0) == '=')
						{
							int lit_index = search_literal_index(token_table[j].operand[0]);
							if (lit_table[lit_index].object != 0)				//�̹� ��ϵǾ� �ִٸ�. ����
								;
							else
							{
								StringTokenizer st = new StringTokenizer(token_table[j].operand[0],"'");
								String tok =st.nextToken();

								if (tok.equals("=C"))
								{
									tok = st.nextToken();
									for (int k = 0; k < tok.length(); k++)
									{
										lit_table[lit_index].object += tok.charAt(k);
										if (k == tok.length() - 1)
											continue;
										lit_table[lit_index].object = lit_table[lit_index].object << 8;
									}
									System.out.printf("%04X	%s	%s		%X\n", locctr, "*", lit_table[lit_index].literal, lit_table[lit_index].object);
									locctr = locctr + tok.length();
								}
								else if (tok.equals("=X"))
								{
									tok = st.nextToken();
									lit_table[lit_index].object = Integer.valueOf(tok,16);
									System.out.printf("%04X	%s	%s		%02X\n", locctr, "*", lit_table[lit_index].literal, lit_table[lit_index].object);
									locctr = locctr + tok.length() / 2;
								}
							}
						}
					}
					ltorg_line = i;					//ltorg ���� ����. ���� ltorg���� ���� ���ͷ��� �����ϵ��� �ؾ��ϱ⶧��
				}
				else
					System.out.printf("	%s	%s	%s\n", token_table[i].label, token_table[i].operator,token_table[i].total_operand);

			}
		}
		return 0;
	}
	
	
	
	
	/* ----------------------------------------------------------------------------------
	* ���� : �Էµ� ���ڿ��� �̸��� ���� ���Ͽ� ���α׷��� ����� �����ϴ� �Լ��̴�.
	*        ���⼭ ��µǴ� ������ object code (������Ʈ 1��) �̴�.
	* �Ű� : ������ ������Ʈ ���ϸ�
	* ��ȯ : ����
	* ���� : ���� ���ڷ� NULL���� ���´ٸ� ���α׷��� ����� ǥ��������� ������
	*        ȭ�鿡 ������ش�.
	*
	* -----------------------------------------------------------------------------------
	*/
	void make_objectcode_output(String file_name)
	{
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(file_name);			
			//PrintWriter Ŭ������ �̿��� ���� ����� ��.(Printwriter Ŭ������ ��� printf�� ���������ν� ���˿� ���� ����� �����Ѵٴ°� �����̴�)
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int index = 0;
		section_num = 0;
		int ltorg_line = 0;
		int change_line_index = 0;
		int current_addr = 0;
		int totalbyte = 0;
		int checkindex = 0;
		int count = 0;
		int firstaddr = 0;

		for (int i = 0; i <= line_num; i++)
		{

			if (token_table[i].operator.equals("START"))
			{
				locctr = Integer.valueOf(token_table[i].operand[0],16);
				firstaddr = locctr;												//H���ڵ� ���� �����ּҸ� �����ϱ� ���� ����
				totalbyte = Integer.valueOf(token_table[i].operand[0],16);
				pw.printf("H%-6s%06X%06X", token_table[i].label, totalbyte, section_length[section_num]);		//H���ڵ� ���
				pw.println("");
			}
			else if (token_table[i].label.equals("."))		//�ּ� ó��, �ּ��� opcode�� ã�� �ʿ䰡 ����.
			{
					continue;
			}

			if ((index = search_opcode(token_table[i].operator)) > -1)		//opcode�� ������ �� ��ū�鿡�� ���� �󸶱��� �о������� ���
			{
				if (inst_table[index].type.equals("1"))	// format 1 �� ��
				{
					count = 1;
					totalbyte = totalbyte + count;
					locctr = locctr + 1;
				}
				else if (inst_table[index].type.equals("2")) // format 2 �� �� operand�� ���� �������Ϳ� ���� ����
				{
					count = 2;
					totalbyte = totalbyte + count;
					locctr = locctr + 2;
				}
				else if (inst_table[index].type.equals("3/4"))
				{
					if (token_table[i].operator.charAt(0) == '+')
					{
						count = 4;
						totalbyte = totalbyte + count;
						locctr = locctr + 4;
					}
					else
					{
						count = 3;
						totalbyte = totalbyte + count;
						locctr = locctr + 3;
					}
				}
			}
			else						// opcode�� ���� directive �� ��
			{
				if (token_table[i].operator.equals("WORD"))
				{
					count = 3;
					totalbyte = totalbyte + count;
					locctr = locctr + 3;
				}
				else if (token_table[i].operator.equals("BYTE"))	//ù ���ڰ� X(16����, ���� 2���� 1����Ʈ), C(���� 1���� 1����Ʈ) �϶� ó��
				{
					StringTokenizer st = new StringTokenizer(token_table[i].total_operand,"'");					
					String tok = st.nextToken();
					
					if (tok.equals("C"))
					{
						tok = st.nextToken();
						count = tok.length();

						totalbyte = totalbyte + count;
						locctr = locctr + count;
					}
					else if (tok.equals("X"))
					{
						tok = st.nextToken();
						count = tok.length()/2;
						totalbyte = totalbyte + count;
						locctr = locctr + count;
					}
				}
				else if (token_table[i].operator.equals("RESW"))
				{
					count = 3 * Integer.parseInt(token_table[i].total_operand);
					locctr = locctr + count;

				}
				else if (token_table[i].operator.equals("RESB"))
				{
					count = Integer.parseInt(token_table[i].total_operand);
					locctr = locctr + count;
				}
				else if (token_table[i].operator.equals("EXTDEF"))
				{
					count = 0;
					pw.printf("D");

					for (int j = 0; j < MAX_OPERAND; j++)
					{
						if (token_table[i].operand[j].equals(""))
							break;
						else
						{
							int addr = search_symbol(token_table[i].operand[j]);				//�� �ּҸ� �� ���ڿ��� �޾ƿ�
							pw.printf("%-6s%06X", token_table[i].operand[j], addr);
						}
					}
					pw.println("");
				}
				else if (token_table[i].operator.equals("EXTREF"))
				{
					count = 0;
					pw.printf("R");

					for (int j = 0; j < MAX_OPERAND; j++)
					{
						if (token_table[i].operand[j].equals(""))
							break;
						else
							pw.printf("%-6s", token_table[i].operand[j]);
					}
					pw.println("");
				}
				else if (token_table[i].operator.equals("LTORG"))			//LTORG �� ���� ���� object �ڵ�� ���, �� �� ���ͷ��� ���� �ڵ� ���� ó��(���ͷ��� CSECT,END���� ���)
				{
					count = 0;
					pw.printf("T%06X%02X", current_addr, totalbyte - count);				
					for (int j = checkindex; j < i; j++)											
					{
						int temp_index = search_opcode(token_table[j].operator);
						if (token_table[j].hasobjectcode == 1)
						{
							if (temp_index == -1)
							{
								if (token_table[j].operator.equals("WORD"))
									pw.printf("%06X", token_table[j].object);
								else
									pw.printf("%02X", token_table[j].object);
							}
							else if (inst_table[temp_index].type.equals("2"))
								pw.printf("%04X", token_table[j].object);
							else
								pw.printf("%06X", token_table[j].object);
						}
					}
					pw.println("");
					
					current_addr += totalbyte - count;
					totalbyte = 0;
					checkindex = i;		//������ ������ ����

					/////////////////////���ͷ� �ڵ�

					current_addr = locctr;
					for (int j = 0; j <= literal_line; j++)
					{
						if (lit_table[j].section == section_num)
						{
							StringTokenizer st = new StringTokenizer(lit_table[j].literal,"'");					
							String tok = st.nextToken();

							if (tok.equals("=C"))
							{
								tok = st.nextToken();
								count = tok.length();
								totalbyte = totalbyte + count;
								locctr = locctr + count;
							}
							else if (tok.equals("=X"))
							{
								tok = st.nextToken();
								count = tok.length()/2;
								totalbyte = totalbyte + count;
								locctr = locctr + count;
							}
						}
					}
				}
				else if (token_table[i].operator.equals("END"))
				{
					
					for (int j = 0; j <= literal_line; j++)
					{
						if (lit_table[j].section == section_num)
						{
							StringTokenizer st = new StringTokenizer(lit_table[j].literal,"'");					
							String tok = st.nextToken();

							if (tok.equals("=C"))
							{
								tok = st.nextToken();
								count = tok.length();
								totalbyte = totalbyte + count;
								locctr = locctr + count;
							}
							else if (tok.equals("=X"))
							{
								tok = st.nextToken();
								count = tok.length()/2;
								totalbyte = totalbyte + count;
								locctr = locctr + count;
							}
						}
					}
					pw.printf("T%06X%02X", current_addr, totalbyte);			

					for (int j = checkindex; j < i; j++)											
					{
						if (token_table[j].label.equals("."))		//�ּ� ó��, �ּ��� opcode�� ã�� �ʿ䰡 ����.
						{
								continue;
						}
						int temp_index = search_opcode(token_table[j].operator);
						if (token_table[j].hasobjectcode == 1)
						{

							if (temp_index == -1)
							{
								if (token_table[j].operator.equals("WORD"))
									pw.printf("%06X", token_table[j].object);
								else
									pw.printf("%02X", token_table[j].object);
							}
							else if (inst_table[temp_index].type.equals("2"))
								pw.printf("%04X", token_table[j].object);
							else
								pw.printf("%06X", token_table[j].object);
						}
					}
					/////////////////////���ͷ� �ڵ� ���

					for (int j = 0; j <= literal_line; j++)
						if (lit_table[j].section == section_num)
							{
							pw.printf("%02X", lit_table[j].object);
							pw.println("");
							}


					for (int j = 0; j < modification_line; j++)						//modification record �� ������ ���
					{
						int x = mod_table[j].index;
						if (mod_table[j].section == section_num)
						{
							if (token_table[x].operator.equals("WORD"))
							{
								int check = 0;
								for (int k = 0; k < token_table[x].total_operand.length(); k++)
								{
									if (token_table[x].total_operand.charAt(k) == '-')						//- �� ������,
									{
										StringTokenizer st = new StringTokenizer(token_table[x].total_operand,"-\n");					
										String tok = st.nextToken();
										int sym_index = search_symbol_index(tok);
										tok = st.nextToken();
										int sym_index2 = search_symbol_index(tok);
										pw.printf("M%06X06%c%s", token_table[x].current_locctr, sym_table[sym_index].sign, sym_table[sym_index].symbol);
										pw.println("");
										pw.printf("M%06X06%c%s", token_table[x].current_locctr, sym_table[sym_index2].sign, sym_table[sym_index2].symbol);
										pw.println("");
										break;
									}
								}
							}
							else
								{
								pw.printf("M%06X05+%s", token_table[x].current_locctr + 1, token_table[x].operand[0]);
								pw.println("");
								}
						}
					}

					if (section_num == 0)
						{
						pw.printf("E%06X", firstaddr);
						pw.println("");
						}
					else
						{
						pw.printf("E");
						pw.println("");
						}

				}
				else if (token_table[i].operator.equals("CSECT"))	//Control section�� �ٲ� . locctr �ʱ�ȭ, section_num���� �迭 ������ ����
				{
					if (hasliteral[section_num]==1)			//���ͷ��� ������ ���
					{
						pw.printf("T%06X%02X", current_addr, totalbyte);

						for (int j = 0; j <= literal_line; j++)
						{
							if (lit_table[j].section == section_num)
							{
								pw.printf("%02X", lit_table[j].object);
							}
						}
						pw.println("");
					}
					else
					{
						count = 0;
						pw.printf("T%06X%02X", current_addr, totalbyte - count);				//������ �ٲ� �� ������� ���Ѱ͵� ���

						for (int j = checkindex; j < i; j++)											
						{
							int temp_index = search_opcode(token_table[j].operator);
							if (token_table[j].hasobjectcode == 1)
							{
								
								if (temp_index == -1)
								{
									if (token_table[j].operator.equals("WORD"))
										pw.printf("%06X", token_table[j].object);
									else
										pw.printf("%02X", token_table[j].object);
								}
								else if (inst_table[temp_index].type.equals("2"))
									pw.printf("%04X", token_table[j].object);
								else
									pw.printf("%06X", token_table[j].object);
							}
						}
						pw.println("");

					}

					for (int j = 0; j < modification_line; j++)
					{
						int x = mod_table[j].index;
						if (mod_table[j].section == section_num)
						{
							if (token_table[x].operator.equals("WORD"))
							{
								int check = 0;
								for (int k = 0; k < token_table[x].total_operand.length(); k++)
								{
									if (token_table[x].total_operand.charAt(k) == '-')						//- �� ������,
									{
										StringTokenizer st = new StringTokenizer(token_table[x].total_operand,"-\n");					
										String tok = st.nextToken();
										int sym_index = search_symbol_index(tok);
										tok = st.nextToken();
										int sym_index2 = search_symbol_index(tok);

										pw.printf("M%06X06%c%s", token_table[x].current_locctr, sym_table[sym_index].sign, sym_table[sym_index].symbol);
										pw.println("");
										pw.printf("M%06X06%c%s", token_table[x].current_locctr, sym_table[sym_index2].sign, sym_table[sym_index2].symbol);
										pw.println("");
										break;

									}
								}
							}
							else
								{
								pw.printf("M%06X05+%s", token_table[x].current_locctr + 1, token_table[x].operand[0]);
								pw.println("");
								}

						}
					}

					if (section_num == 0)
						{
						pw.printf("E%06X", firstaddr);
						pw.println("");
						}
					else
						{
						pw.printf("E");
						pw.println("");
						}
					

					locctr = 0;
					section_num++;
					totalbyte = 0;
					checkindex = i;
					current_addr = 0;
					pw.println("");
					pw.printf("H%-6s%06X%06X", token_table[i].label, locctr, section_length[section_num]);
					pw.println("");
				}
			}

			if (totalbyte >= 30)
			{
				pw.printf("T%06X%02X", current_addr, totalbyte - count);					//�����ּ�, ������� ���Ͽ� �� ����Ʈ
				for (int j = checkindex; j < i; j++)											//30����Ʈ�� �ѱ������� ���
				{
					if (token_table[j].label.equals("."))		//�ּ� ó��, �ּ��� opcode�� ã�� �ʿ䰡 ����.
					{
							continue;
					}
					int temp_index = search_opcode(token_table[j].operator);
					if (token_table[j].hasobjectcode == 1)
					{
						if (temp_index == -1)
						{
							if (token_table[j].operator.equals("WORD"))
								pw.printf("%06X", token_table[j].object);
							else
								pw.printf("%02X", token_table[j].object);
						}
						else if (inst_table[temp_index].type.equals("2"))
							pw.printf("%04X", token_table[j].object);
						else
							pw.printf("%06X", token_table[j].object);
					}
				}
				pw.println("");

				current_addr += totalbyte - count;
				totalbyte = count;
				checkindex = i;
			}
		}
		pw.flush();					// �ڹ��� ��Ʈ���� flush�� ����� ����� ����
		pw.close();					// ������ ��Ʈ���� �ݾ���.
	}

}
