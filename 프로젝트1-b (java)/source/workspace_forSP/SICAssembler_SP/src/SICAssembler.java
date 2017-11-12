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
		for(int i = 0 ; i<100;i++)							//modification unit 인스턴스 할당
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
	* 설명 : 프로그램 초기화를 위한 자료구조 생성 및 파일을 읽는 함수이다.
	* 매개 : 없음
	* 반환 : 정상종료 = 0 , 에러 발생 = -1
	* 주의 : 각각의 명령어 테이블을 내부에 선언하지 않고 관리를 용이하게 하기
	*		   위해서 파일 단위로 관리하여 프로그램 초기화를 통해 정보를 읽어 올 수 있도록
	*		   구현하였다.
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
	* 설명 : 머신을 위한 기계 코드목록 파일을 읽어 기계어 목록 테이블(inst_table)을
	*        생성하는 함수이다.
	* 매개 : 기계어 목록 파일
	* 반환 : 정상종료 = 0 , 에러 < 0
	* 주의 : 기계어 목록파일 형식은 자유롭게 구현한다. 예시는 다음과 같다.
	*
	*	===============================================================================
	*		   | 이름 | 형식 | 기계어 코드 | 오퍼랜드의 갯수 | NULL|
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
				StringTokenizer st = new StringTokenizer(scanner.nextLine(),"\t\n");		//스캐너 클래스를 이용해 한줄씩 받아옴 + 탭,개행으로 토크나이즈
				
				inst_table[i] = new inst_unit();

				inst_table[i].operator = st.nextToken();
				inst_table[i].type = st.nextToken();
				inst_table[i].opcode = st.nextToken();
				inst_table[i].operandAmount = Integer.parseInt(st.nextToken());
				
				inst_index = i;			//0부터 갯수 시작

				i++;
			}
			scanner.close();
		}
		catch (FileNotFoundException e) {				//파일이 없으면 try catch 로 예외처리
			System.out.println("The inst_file doesn't exist");
			e.printStackTrace();
			return errno = -1;
		}
		return errno;
	}
	
	/* ----------------------------------------------------------------------------------
	* 설명 : 어셈블리 할 소스코드를 읽어오는 함수이다. 읽어오면서 바로  token 을 분리하여 테이블에 넣어준다.
	* 매개 : 어셈블리할 소스파일명
	* 반환 : 정상종료 = 0 , 에러 < 0
	* 주의 :
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
				
			
				token_line = i;	//0부터 시작
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
	* 설명 : 입력 문자열이 기계어 코드인지를 검사하는 함수이다.
	* 매개 : 토큰 단위로 구분된 문자열
	* 반환 : 정상종료 = 기계어 테이블 인덱스, 에러 < 0
	* 주의 :
	*
	* ----------------------------------------------------------------------------------
	*/
	int search_opcode(String str)	//str을 비교값으로 해서 inst_table을 모두 뒤져서 index를 찾음. 없으면 -1 리턴
	{
		String compare;

		if (str.charAt(0) == '+')					//4형식 처리 +를 무시
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
	* 설명 : 심볼들을 비교하여 없으면 테이블에 추가하고 있으면 에러발생
	* 매개 : 비교할 symbol
	* 반환 : symbol이 없어서 추가한 후 종료 = 0, 이미 symbol이 존재하면 -1
	* -----------------------------------------------------------------------------------
	*/
	int manage_symbol(String str)
	{
		for (int i = 0; i <= symbol_line; i++)
		{
			if ((str.equals(sym_table[i].symbol)) && section_num == sym_table[i].section)	//symbol이 있으면서 그 symbol이 같은섹션일때 
				return -1;
		}
		
		//symbol이 없으면 symtab 에 추가, line 수 증가
		sym_table[symbol_line + 1] = new symbol_unit();		//C와 달리 인스턴스를 생성해줘야 하므로 생성
		sym_table[symbol_line + 1].symbol= str;
		sym_table[symbol_line + 1].addr = locctr;
		sym_table[symbol_line + 1].section = section_num;
		sym_table[symbol_line + 1].sign = '+';
		symbol_line++;
		return 0;

	}

	/* ----------------------------------------------------------------------------------
	* 설명 : 리터럴들을 비교하여 없으면 테이블에 추가하고 있으면 에러발생
	* 매개 : 비교할 literal
	* 반환 : literal이 없어서 추가한 후 종료 = 0, 이미 literal이 존재하면 -1
	* -----------------------------------------------------------------------------------
	*/
	int manage_literal(String str)
	{
		for (int i = 0; i <= literal_line; i++)
		{
			if (str.equals(lit_table[i].literal))// && section_num == lit_table[i].section)	//literal이 있으면서 그 literal이 같은섹션일때 
				return -1;
		}

		//literal이 없으면 littab 에 추가, line 수 증가
		lit_table[literal_line+1] = new literal_unit();		//C와 달리 인스턴스를 생성해줘야 하므로 생성
		lit_table[literal_line + 1].literal =  str;
		lit_table[literal_line + 1].addr = locctr;
		lit_table[literal_line + 1].object = 0;
		lit_table[literal_line + 1].section = section_num;
		hasliteral[section_num] = 1;					// 각 섹션에서 리터럴이 쓰이는 지 없는지 판별에 도움을 주는 배열 1-> 리터럴이 섹션에 있음, 2-> 섹션에 없음
		literal_line++;
		return 0;

	}

	/* ----------------------------------------------------------------------------------
	* 설명 : 심볼들을 비교하여 있으면 주소 반환, 다른 경우 에러 숫자 반환
	* 매개 : 비교할 문자열
	* 반환 : symbol이 존재하고, 같은섹션에 있을 때 = 심볼에 대응되는 주소값 반환,
			 symbol이 존재하나 다른 섹션에 있을때 = -2 반환,
			 symbol이 존재 하지않다면 -1로 에러 반환
	* -----------------------------------------------------------------------------------
	*/
	int search_symbol(String str)
	{
		int addr = -1;
		for (int i = 0; i <= symbol_line; i++)
		{
			if (str.equals(sym_table[i].symbol))
			{
				if (section_num == sym_table[i].section)	//symbol이 있으면서 그 symbol이 같은섹션일때 
					return sym_table[i].addr;
				else										//symbol이 있으나 같은 섹션이 아닐때는 -2 반환
					addr = -2;
			}
		}

		return addr;
	}

	/* ----------------------------------------------------------------------------------
	* 설명 : 문자열과 리터럴 테이블의 값들 비교
	* 매개 : 비교할 문자열
	* 반환 : literal이 존재함 = 그 리터럴의 주소값 반환,
			 이미 literal이 존재하지 않으면 -1로 에러 반환
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
	* 설명 : 입력 문자열의 리터럴 인덱스를 찾는 함수이다.
	* 매개 : 비교할 문자열
	* 반환 : 정상종료 = 리터럴 테이블 인덱스, 에러 < 0
	* ----------------------------------------------------------------------------------
	*/
	int search_literal_index(String str)	//str을 비교값으로 해서 inst_table을 모두 뒤져서 index를 찾음. 없으면 -1 리턴
	{
		for (int i = 0; i <= literal_line; i++)
		{
			if (str.equals(lit_table[i].literal))
				return i;
		}

		return -1;
	}

	/* ----------------------------------------------------------------------------------
	* 설명 : 입력 문자열의 심볼 인덱스를 찾는 함수이다.
	* 매개 : 비교할 문자열
	* 반환 : 정상종료 = 심볼 테이블 인덱스, 에러 < 0
	* ----------------------------------------------------------------------------------
	*/
	int search_symbol_index(String str)	//str을 비교값으로 해서 inst_table을 모두 뒤져서 index를 찾음. 없으면 -1 리턴
	{
		for (int i = 0; i <= symbol_line; i++)
		{
			if (str.equals(sym_table[i].symbol))
				return i;
		}

		return -1;
	}
	
	
	
	/* ----------------------------------------------------------------------------------
	* 설명 : 어셈블리 코드를 위한 패스1과정을 수행하는 함수이다.
	*		   패스1에서는..
	*		   1. 프로그램 소스를 스캔하여 해당하는 토큰단위로 분리하여 프로그램 라인별 토큰
	*		   테이블을 생성한다.
	*
	* 매개 : 없음
	* 반환 : 정상 종료 = 0 , 에러 = < 0
	* 주의 : 현재 초기 버전에서는 에러에 대한 검사를 하지 않고 넘어간 상태이다.
	*	  따라서 에러에 대한 검사 루틴을 추가해야 한다.
	*
	* -----------------------------------------------------------------------------------
	*/
	int assem_pass1()
	{
		
		int index = 0;
		int ltorg_line = 0;
		for (int i = 0; i <= line_num; i++)
		{

			if (token_table[i].operator.equals("START"))		//START 면 LOCCTR 을 그 인자로 초기화
				{
				locctr = Integer.valueOf(token_table[i].operand[0],16);
				}
			else if (token_table[i].label.equals( "."))		//주석 처리, 주석은 opcode를 찾을 필요가 없음.
				continue;


			token_table[i].current_locctr = locctr;		//각 토큰에 locctr 기록

			if (token_table[i].operator.equals("CSECT"))	//Control section이 바뀜 -> locctr 초기화, section_num으로 배열 내에서 구분
			{
				section_length[section_num] = locctr;			//각 섹션의 길이 저장해둠
				locctr = 0;
				section_num++;
				continue;
			}

			if (!token_table[i].label.equals("")) // 라벨이 있으면	심볼 처리
				if (manage_symbol(token_table[i].label) < 0)
					return -1;										//같은 섹션 내에 같은 이름의 라벨이 존재하면 에러 출력

			if ((index = search_opcode(token_table[i].operator)) > -1)		//opcode가 있을때, 이때만 pc값 기록, index로 테이블에 접근한다
			{
				token_table[i].hasobjectcode = 1;							//object코드를 생성하는 토큰임을 기록
				if (inst_table[index].type.equals("1"))	// format 1 일 때
				{
					locctr = locctr + 1;
					token_table[i].pcValue = locctr;

				}
				else if (inst_table[index].type.equals("2")) // format 2 일 때
				{
					locctr = locctr + 2;
					token_table[i].pcValue = locctr;
				}
				else if (inst_table[index].type.equals("3/4")) // format 3/4 일 때 +여부로 locctr 증가되는 수 정해줌
				{
					if (token_table[i].operator.charAt(0) == '+')
						locctr = locctr + 4;
					else
						locctr = locctr + 3;

					token_table[i].pcValue = locctr;
				}
			}
			else						// opcode가 없는 directive 일 때
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
				else if (token_table[i].operator.equals("BYTE"))	//첫 글자가 X(16진수, 문자 2개당 1바이트), C(문자 1개당 1바이트) 일때 처리
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
							if (manage_literal(token_table[j].operand[0]) < 0)		//이미 있는 리터럴을 무시
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

					ltorg_line = i;					//ltorg 라인 관리. 이전 ltorg에서 나온 리터럴은 무시하도록 해야하기때문
				}
				else if (token_table[i].operator.equals("END"))
				{
					for (int j = ltorg_line; j < i; j++)
					{
						if (token_table[j].operand[0].charAt(0) == '=')
						{
							if (manage_literal(token_table[j].operand[0]) < 0)		//이미 있는 리터럴을 무시
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

					ltorg_line = i;					//ltorg 라인 관리. 이전 ltorg에서 나온 리터럴은 무시하도록 해야하기때문
				}
			}
		}
		
		return 0;
	}
	
	/* ----------------------------------------------------------------------------------
	* 설명 : 어셈블리 코드를 기계어 코드로 바꾸기 위한 패스2 과정을 수행하는 함수이다.
	*		   패스 2에서는 프로그램을 기계어로 바꾸는 작업은 라인 단위로 수행된다.
	*		   다음과 같은 작업이 수행되어 진다.
	*		   1. 실제로 해당 어셈블리 명령어를 기계어로 바꾸는 작업을 수행한다.
	* 매개 : 없음
	* 반환 : 정상종료 = 0, 에러발생 = < 0
	* 주의 :
	* -----------------------------------------------------------------------------------
	*/
	int assem_pass2()
	{
		int index = 0;
		section_num = 0;
		int ltorg_line = 0;

		for (int i = 0; i <= line_num; i++)
		{
			if (token_table[i].operator.equals("START"))		//START 면 LOCCTR 을 그 인자로 초기화
			{
				locctr = Integer.valueOf(token_table[i].operand[0],16);

				System.out.printf("%04X	%s	%s	%s\n", locctr, token_table[i].label, token_table[i].operator,token_table[i].total_operand);
				continue;
			}
			else if (token_table[i].label.equals("."))		//주석 처리, 주석은 opcode를 찾을 필요가 없음.
			{
				System.out.printf("%s	%s	%s	%s\n", token_table[i].label, token_table[i].operator,token_table[i].total_operand, token_table[i].comment);

				continue;
			}


			if (token_table[i].operator.equals("CSECT"))	//Control section이 . -> locctr 초기화, section_num으로 배열 내에서 구분
			{
				locctr = 0;
				section_num++;
			}

			if ((index = search_opcode(token_table[i].operator)) > -1)		//opcode가 있을때, 이때만 pc값 기록
			{
				if (inst_table[index].type.equals("1"))	// format 1 일 때
				{
					token_table[i].object = Integer.valueOf(inst_table[index].opcode,16); 

					System.out.printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i].label, token_table[i].operator,token_table[i].total_operand, token_table[i].object);
					locctr = locctr + 1;
				}
				else if (inst_table[index].type.equals("2")) // format 2 일 때 operand로 오는 레지스터에 따라 구분
				{
					token_table[i].object = Integer.valueOf(inst_table[index].opcode,16); 
					
					for (int j = 0; j < 2; j++)
					{
						token_table[i].object = token_table[i].object << 4;			//주소계산. opcode를 쓴 후 4비트씩 밀면서 operand들 기록

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

					if (token_table[i].operator.charAt(0) == '+')				//4형식
					{
						token_table[i].nixbpe += N + I + E;					//NIXBPE중에 4형식은 NIE 기본, X가 추가될수 있으므로 값 할당
						if (token_table[i].operand[1].equals("X"))
							token_table[i].nixbpe += X;

						target_addr = search_symbol(token_table[i].operand[0]);
						if (target_addr == -2)							//라벨이 있으나 다른 섹션에 있음. M레코드에 써야하므로 구조체에 기록
						{
							mod_table[modification_line].index = i;
							mod_table[modification_line].section = section_num;
							modification_line++;
							target_addr = 0;
						}

						token_table[i].object = Integer.valueOf(inst_table[index].opcode,16) + (token_table[i].nixbpe >> 4);		//nixpbe의 상위 2비트만 사용
						token_table[i].object = (token_table[i].object << 4) + (token_table[i].nixbpe & 15);						//15와 & 연산 .하위 4비트 만 사용
						token_table[i].object = (token_table[i].object << 20) + target_addr;										//앞의 (16진수로)3자리 계산후 5자리 뒤로 밈 그후 타겟주소 계산

						System.out.printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i].label, token_table[i].operator,token_table[i].total_operand, token_table[i].object);

						locctr = locctr + 4;
					}
					else												//3형식
					{
						if (token_table[i].operand[0].charAt(0) == '#')					//immediate	addressing
						{
							String temp = token_table[i].operand[0].substring(1);
							target_addr = Integer.valueOf(temp,10);					//immediate은 10진수 이므로

							token_table[i].nixbpe += I;							// I  비트만 사용

							token_table[i].object = Integer.valueOf(inst_table[index].opcode,16) + (token_table[i].nixbpe >> 4);		//nixpbe의 상위 2비트만 사용
							token_table[i].object = (token_table[i].object << 4) + (token_table[i].nixbpe & 15);						//15와 & 연산 .하위 4비트 만 사용
							token_table[i].object = (token_table[i].object << 12) + target_addr;										//앞의 (16진수로)3자리 계산후 3자리 뒤로 밈 그후 타겟주소 계산

							System.out.printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i].label, token_table[i].operator,token_table[i].total_operand, token_table[i].object);

							locctr = locctr + 3;						
							continue;
						}
						else if (token_table[i].operand[0].charAt(0) == '@')								//indirect addressing
						{
							String temp = token_table[i].operand[0].substring(1);
							target_addr = search_symbol(temp);
							dest = target_addr - token_table[i].pcValue;							//target addr - pc 값

							token_table[i].nixbpe += N + P;

							token_table[i].object = Integer.valueOf(inst_table[index].opcode,16) + (token_table[i].nixbpe >> 4);		//nixpbe의 상위 2비트만 사용
							token_table[i].object = (token_table[i].object << 4) + (token_table[i].nixbpe & 15);						//15와 & 연산 .하위 4비트 만 사용
							token_table[i].object = (token_table[i].object << 12) + dest;												//앞의 (16진수로)3자리 계산후 3자리 뒤로 밈 그후 dest 주소 계산

							System.out.printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i].label, token_table[i].operator,token_table[i].total_operand, token_table[i].object);

							locctr = locctr + 3;
						}
						else																		//direct addressing
						{
							if (token_table[i].operand[0].charAt(0) == '=')						//리터럴
							{
								target_addr = search_literal(token_table[i].operand[0]);
							}
							else
							{
								if (inst_table[index].operandAmount == 0)					//operand가 없을 때
								{
									target_addr = 0;										//RSUB 같은 operand가 없는 라인은 주소를 0으로 해줌

									token_table[i].nixbpe += N + I;

									token_table[i].object = Integer.valueOf(inst_table[index].opcode,16) + (token_table[i].nixbpe >> 4);
									token_table[i].object = (token_table[i].object << 4) + (token_table[i].nixbpe & 15);		//15와 & 연산 .하위 4비트 만 사용
									token_table[i].object = (token_table[i].object << 12) + target_addr;
									//System.out.println(""+locctr+"	"+token_table[i].label + "	" + token_table[i].operator + "	"+token_table[i].total_operand + "	"+token_table[i].object);

									System.out.printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i].label, token_table[i].operator,token_table[i].total_operand, token_table[i].object);

									locctr = locctr + 3;
									continue;
								}
								else														//operand가 있을 때
									target_addr = search_symbol(token_table[i].operand[0]);
							}


							if (target_addr == -1)	//에러 발생 symbol을 쓰려는데 없음.
								return -1;
							else	//타겟 주소를 받아옴.
							{
								dest = target_addr - token_table[i].pcValue;
								if (-2048 <= dest && dest <= 2047)			//PC relative 방식 범위 내에 존재해야함
								{
									token_table[i].nixbpe += N + I + P;

									token_table[i].object = Integer.valueOf(inst_table[index].opcode,16) + (token_table[i].nixbpe >> 4);
									token_table[i].object = (token_table[i].object << 4) + (token_table[i].nixbpe & 15);		//15와 & 연산 ->하위 4비트 만 사용
									if (dest < 0)
									{
										token_table[i].object = (token_table[i].object << 12) + (dest & 0x00000FFF);		//음수일 경우 하위 3바이트만 주소로 사용해야하므로 비트연산함
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
			else						// opcode가 없는 directive 일 때
			{
				if (token_table[i].operator.equals("WORD"))
				{
					int check = 0;
					for (int j = 0; j < token_table[i].operand[0].length(); j++)
					{
						if (token_table[i].operand[0].charAt(j) == '-')						//- 가 있을떄,
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
						else if (token_table[i].operand[0].charAt(j) == '+')					// + 가 있을때
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
					if (check == 0)														// 두 부호가 존재하지 않을때, 즉 계산하지 않아도될때
					{
						token_table[i].object = 3 * Integer.parseInt(token_table[i].operand[0]);
						System.out.printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i].label, token_table[i].operator,token_table[i].total_operand, token_table[i].object);

						locctr = locctr + 3;
					}
				}
				else if (token_table[i].operator.equals("BYTE"))	//첫 글자가 X(16진수, 문자 2개당 1바이트), C(문자 1개당 1바이트) 일때 처리
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
							token_table[i].object = token_table[i].object << 8;					//바이트 수 만큼 object 코드 쉬프트함
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
							if (lit_table[lit_index].object != 0)				//이미 기록되어 있다면. 무시
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
					ltorg_line = i;					//ltorg 라인 관리. 이전 ltorg에서 나온 리터럴은 무시하도록 해야하기때문
				}
				else
					System.out.printf("	%s	%s	%s\n", token_table[i].label, token_table[i].operator,token_table[i].total_operand);

			}
		}
		return 0;
	}
	
	
	
	
	/* ----------------------------------------------------------------------------------
	* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
	*        여기서 출력되는 내용은 object code (프로젝트 1번) 이다.
	* 매개 : 생성할 오브젝트 파일명
	* 반환 : 없음
	* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
	*        화면에 출력해준다.
	*
	* -----------------------------------------------------------------------------------
	*/
	void make_objectcode_output(String file_name)
	{
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(file_name);			
			//PrintWriter 클래스를 이용해 파일 출력을 함.(Printwriter 클래스의 경우 printf를 지원함으로써 포맷에 따른 출력을 지원한다는게 장점이다)
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
				firstaddr = locctr;												//H레코드 에서 시작주소를 저장하기 위해 저장
				totalbyte = Integer.valueOf(token_table[i].operand[0],16);
				pw.printf("H%-6s%06X%06X", token_table[i].label, totalbyte, section_length[section_num]);		//H레코드 기록
				pw.println("");
			}
			else if (token_table[i].label.equals("."))		//주석 처리, 주석은 opcode를 찾을 필요가 없음.
			{
					continue;
			}

			if ((index = search_opcode(token_table[i].operator)) > -1)		//opcode가 있을때 각 토큰들에선 현재 얼마까지 읽었는지만 기록
			{
				if (inst_table[index].type.equals("1"))	// format 1 일 때
				{
					count = 1;
					totalbyte = totalbyte + count;
					locctr = locctr + 1;
				}
				else if (inst_table[index].type.equals("2")) // format 2 일 때 operand로 오는 레지스터에 따라 구분
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
			else						// opcode가 없는 directive 일 때
			{
				if (token_table[i].operator.equals("WORD"))
				{
					count = 3;
					totalbyte = totalbyte + count;
					locctr = locctr + 3;
				}
				else if (token_table[i].operator.equals("BYTE"))	//첫 글자가 X(16진수, 문자 2개당 1바이트), C(문자 1개당 1바이트) 일때 처리
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
							int addr = search_symbol(token_table[i].operand[j]);				//각 주소를 비교 문자열로 받아옴
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
				else if (token_table[i].operator.equals("LTORG"))			//LTORG 위 라인 까지 object 코드로 출력, 그 후 리터럴에 대한 코드 정보 처리(리터럴은 CSECT,END에서 출력)
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
					checkindex = i;		//어디까지 읽은지 저장

					/////////////////////리터럴 코드

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
						if (token_table[j].label.equals("."))		//주석 처리, 주석은 opcode를 찾을 필요가 없음.
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
					/////////////////////리터럴 코드 출력

					for (int j = 0; j <= literal_line; j++)
						if (lit_table[j].section == section_num)
							{
							pw.printf("%02X", lit_table[j].object);
							pw.println("");
							}


					for (int j = 0; j < modification_line; j++)						//modification record 가 있을때 출력
					{
						int x = mod_table[j].index;
						if (mod_table[j].section == section_num)
						{
							if (token_table[x].operator.equals("WORD"))
							{
								int check = 0;
								for (int k = 0; k < token_table[x].total_operand.length(); k++)
								{
									if (token_table[x].total_operand.charAt(k) == '-')						//- 가 있을떄,
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
				else if (token_table[i].operator.equals("CSECT"))	//Control section이 바뀜 . locctr 초기화, section_num으로 배열 내에서 구분
				{
					if (hasliteral[section_num]==1)			//리터럴이 있을때 출력
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
						pw.printf("T%06X%02X", current_addr, totalbyte - count);				//섹션이 바뀔때 다 출력하지 못한것들 출력

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
									if (token_table[x].total_operand.charAt(k) == '-')						//- 가 있을떄,
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
				pw.printf("T%06X%02X", current_addr, totalbyte - count);					//시작주소, 현재까지 파일에 쓴 바이트
				for (int j = checkindex; j < i; j++)											//30바이트가 넘기전까지 출력
				{
					if (token_table[j].label.equals("."))		//주석 처리, 주석은 opcode를 찾을 필요가 없음.
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
		pw.flush();					// 자바의 스트림은 flush를 해줘야 출력을 해줌
		pw.close();					// 열었던 스트림을 닫아줌.
	}

}
