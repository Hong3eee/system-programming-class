/*
* 화일명 : my_assembler.c
* 설  명 : 이 프로그램은 SIC/XE 머신을 위한 간단한 Assembler 프로그램의 메인루틴으로,
* 입력된 파일의 코드 중, 명령어에 해당하는 OPCODE를 찾아 출력한다.
*
*/

/*
*
* 프로그램의 헤더를 정의한다.
*
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>

#include "my_assembler_20122366.h"

/* ----------------------------------------------------------------------------------
* 설명 : 사용자로 부터 어셈블리 파일을 받아서 명령어의 OPCODE를 찾아 출력한다.
* 매개 : 실행 파일, 어셈블리 파일
* 반환 : 성공 = 0, 실패 = < 0
* 주의 : 현재 어셈블리 프로그램의 리스트 파일을 생성하는 루틴은 만들지 않았다.
*		   또한 중간파일을 생성하지 않는다.
* ----------------------------------------------------------------------------------
*/

char * buff;		// inst_table을 만들때 사용하는 버퍼


int main(int args, char *arg[])
{
	if (init_my_assembler() < 0)
	{
		printf("init_my_assembler: error.\n");
		return -1;
	}

	if (assem_pass1() < 0) {
		printf("assem_pass1: error.  \n");
		return -1;
	}
	if (assem_pass2() < 0) {
		printf(" assem_pass2: error.  \n");
		return -1;
	}
	make_objectcode_output("output_20122366.txt");

	return 0;
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
int init_my_assembler(void)
{
	int result;

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
int init_inst_file(char *inst_file)
{
	FILE * file;
	int errno;
	int i = 0;
	/* add your code here */

	if ((file = fopen(inst_file, "r")) == NULL)
	{
		printf("The inst_file doesn't exist\n");
		return errno = -1;
	}

	while (!feof(file))				//파일의 끝까지
	{
		buff = (char *)malloc(1024);
		fgets(buff, 1024, file);

		char * tok = strtok(buff, "	");

		inst_table[i] = (token *)malloc(1024);

		inst_table[i]->operator = tok;
		tok = strtok(NULL, "	");

		inst_table[i]->type = tok;
		tok = strtok(NULL, "	");

		inst_table[i]->opcode = tok;
		tok = strtok(NULL, "	");

		inst_table[i]->operandAmount = atoi(tok);
		tok = strtok(NULL, "	");

		//printf("%s	%s	%s	%d\n", inst_table[i]->operator, inst_table[i]->type,inst_table[i]->opcode, inst_table[i]->operandAmount); //콘솔에 확인

		inst_index = i;			//0부터 갯수 시작

		i++;
	}

	buff = (char *)malloc(1024); // 메모리 마지막 공간 새로 할당해서 침범 방지......

	fclose(file);


	return errno;
}

/* ----------------------------------------------------------------------------------
* 설명 : 어셈블리 할 소스코드를 읽어오는 함수이다.
* 매개 : 어셈블리할 소스파일명
* 반환 : 정상종료 = 0 , 에러 < 0
* 주의 :
*
* ----------------------------------------------------------------------------------
*/
int init_input_file(char *input_file)
{
	FILE * file;
	int errno;
	int i = 0;

	/* add your code here */

	if ((file = fopen(input_file, "r")) == NULL)
	{
		printf("The input_file doesn't exist\n");
		return errno = -1;
	}

	while (!feof(file))
	{
		input_data[i] = (char *)malloc(1024);
		fgets(input_data[i], 1024, file);
		line_num = i;

		if (token_parsing(i) < 0)
		{
			printf("Token Parsing Error occured!\n");
			return errno = -1;
		}

		i++;
	}
	fclose(file);



	return errno;
}

/* ----------------------------------------------------------------------------------
* 설명 : 소스 코드를 읽어와 토큰단위로 분석하고 토큰 테이블을 작성하는 함수이다.
*        패스 1로 부터 호출된다.
* 매개 : 소스코드의 라인번호
* 반환 : 정상종료 = 0 , 에러 < 0
* 주의 : my_assembler 프로그램에서는 라인단위로 토큰 및 오브젝트 관리를 하고 있다.
* ----------------------------------------------------------------------------------
*/
int token_parsing(int index)
{
	/* add your code here */

	int i = 0;
	char * tok = strtok(input_data[line_num], "\t\n");
	char * tok2;

	char first = input_data[line_num][0];
	token_table[index] = (token *)malloc(1024);
	if (first == '\t')				//label이 없을 경우를 처리
	{
		token_table[index]->label = "";
	}
	else
	{
		token_table[index]->label = tok;
		tok = strtok(NULL, "\t\n");
	}
	if (tok == NULL)
		tok = "";

	token_table[index]->operator = tok;

	tok = strtok(NULL, "\t\n");
	if (tok == NULL)
		tok = "";

	token_table[index]->operand[0] = tok;

	///////////////////////////////////

	strcpy(token_table[index]->total_operand, token_table[index]->operand[0]);

	token_table[index]->operand[1] = "";
	token_table[index]->operand[2] = "";

	tok2 = strtok(token_table[index]->operand[0], ",\t");				// ',','\t' 으로 구분해서 operand를 각 index 위치에 할당

	for (int j = 1; j < 3; j++)
	{
		tok2 = strtok(NULL, ",\t");
		if (tok2 == NULL)
			break;
		else
		{
			token_table[index]->operand[j] = tok2;
		}
	}
	/////////////////////////////////////

	tok = strtok(NULL, "\t\n");
	if (tok == NULL)
		tok = "";
	token_table[index]->comment = tok;

	token_table[index]->nixbpe = 0; //bit들 0으로 초기화
	token_table[index]->object = 0;
	token_table[index]->hasobjectcode = 0;			//object코드를 만드는 라인인지 확인 0 -> 안만듦 1 -> 만듦
	token_line = index;		//0부터 시작

}

/* ----------------------------------------------------------------------------------
* 설명 : 입력 문자열이 기계어 코드인지를 검사하는 함수이다.
* 매개 : 토큰 단위로 구분된 문자열
* 반환 : 정상종료 = 기계어 테이블 인덱스, 에러 < 0
* 주의 :
*
* ----------------------------------------------------------------------------------
*/
int search_opcode(char *str)	//str을 비교값으로 해서 inst_table을 모두 뒤져서 index를 찾음. 없으면 -1 리턴
{
	/* add your code here */
	char compare[100];

	if (str[0] == '+')					//4형식 처리 +를 무시
		strcpy(compare, &str[1]);
	else
		strcpy(compare, str);


	for (int i = 0; i <= inst_index; i++)
	{
		if (!strcmp(compare, inst_table[i]->operator))
			return i;
	}

	return -1;
}

/* ----------------------------------------------------------------------------------
* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
*        여기서 출력되는 내용은 명령어 옆에 OPCODE가 기록된 표(과제 4번) 이다.
* 매개 : 생성할 오브젝트 파일명
* 반환 : 없음
* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
*        화면에 출력해준다.
*        또한 과제 4번에서만 쓰이는 함수이므로 이후의 프로젝트에서는 사용되지 않는다.
* -----------------------------------------------------------------------------------
*/
void make_opcode_output(char *file_name)
{
	/* add your code here */
	int index = 0;
	FILE * file = fopen(file_name, "w");

	for (int i = 0; i <= token_line; i++)
	{

		if (!strcmp(token_table[i]->label, "."))		//주석 처리, 주석은 opcode를 찾을 필요가 없음.
		{
			fprintf(file, "%s	%s	%s	%s\n", token_table[i]->label, token_table[i]->operator,token_table[i]->operand[0], token_table[i]->comment);
			continue;
		}

		if ((index = search_opcode(token_table[i]->operator)) > -1)		//opcode가 있을때
		{
			if (inst_table[index]->operandAmount == 0)		// format 1 일 때
				fprintf(file, "%s	%s	%s	%s\n", token_table[i]->label, token_table[i]->operator,"", inst_table[index]->opcode);

			else
				fprintf(file, "%s	%s	%s	%s\n", token_table[i]->label, token_table[i]->operator,token_table[i]->operand[0], inst_table[index]->opcode);
		}
		else						// opcode가 없는 directive 일 때
			fprintf(file, "%s	%s	%s\n", token_table[i]->label, token_table[i]->operator,token_table[i]->operand[0], token_table[i]->comment);

	}
	fclose(file);

}




/* --------------------------------------------------------------------------------*
* ------------------------- 추후 프로젝트에서 사용할 함수 --------------------------*
* --------------------------------------------------------------------------------*/

/* ----------------------------------------------------------------------------------
* 설명 : 심볼들을 비교하여 없으면 테이블에 추가하고 있으면 에러발생
* 매개 : 비교할 symbol
* 반환 : symbol이 없어서 추가한 후 종료 = 0, 이미 symbol이 존재하면 -1
* -----------------------------------------------------------------------------------
*/
int manage_symbol(char *str)
{
	/* add your code here */
	for (int i = 0; i <= symbol_line; i++)
	{
		if ((!strcmp(str, sym_table[i].symbol)) && section_num == sym_table[i].section)	//symbol이 있으면서 그 symbol이 같은섹션일때 
			return -1;
	}
	
	//symbol이 없으면 symtab 에 추가, line 수 증가
	strcpy(sym_table[symbol_line + 1].symbol, str);
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
int manage_literal(char *str)
{
	/* add your code here */
	for (int i = 0; i <= literal_line; i++)
	{
		if ((!strcmp(str, lit_table[i].literal)))// && section_num == lit_table[i].section)	//literal이 있으면서 그 literal이 같은섹션일때 
			return -1;
	}

	//literal이 없으면 littab 에 추가, line 수 증가
	strcpy(lit_table[literal_line + 1].literal, str);
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
int search_symbol(char *str)
{
	int addr = -1;
	/* add your code here */
	for (int i = 0; i <= symbol_line; i++)
	{
		if ((!strcmp(str, sym_table[i].symbol)))
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
int search_literal(char *str)
{
	/* add your code here */
	for (int i = 0; i <= literal_line; i++)
	{
		if ((!strcmp(str, lit_table[i].literal)))
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
int search_literal_index(char *str)	//str을 비교값으로 해서 inst_table을 모두 뒤져서 index를 찾음. 없으면 -1 리턴
{
	for (int i = 0; i <= literal_line; i++)
	{
		if (!strcmp(str, lit_table[i].literal))
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
int search_symbol_index(char *str)	//str을 비교값으로 해서 inst_table을 모두 뒤져서 index를 찾음. 없으면 -1 리턴
{
	for (int i = 0; i <= symbol_line; i++)
	{
		if (!strcmp(str, sym_table[i].symbol))
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
static int assem_pass1(void)
{
	/* add your code here */
	int index = 0;
	int ltorg_line = 0;
	for (int i = 0; i <= line_num; i++)
	{
		if (!strcmp(token_table[i]->operator,"START"))		//START 면 LOCCTR 을 그 인자로 초기화
			locctr = strtol(token_table[i]->operand, NULL, 16);
		else if (!strcmp(token_table[i]->label, "."))		//주석 처리, 주석은 opcode를 찾을 필요가 없음.
			continue;


		token_table[i]->current_locctr = locctr;		//각 토큰에 locctr 기록

		if (!strcmp(token_table[i]->operator,"CSECT"))	//Control section이 바뀜 -> locctr 초기화, section_num으로 배열 내에서 구분
		{
			section_length[section_num] = locctr;			//각 섹션의 길이 저장해둠
			locctr = 0;
			section_num++;
			continue;
		}

		if (strcmp(token_table[i]->label, "")) // 라벨이 있으면	심볼 처리
			if (manage_symbol(token_table[i]->label) < 0)
				return -1;										//같은 섹션 내에 같은 이름의 라벨이 존재하면 에러 출력


		if ((index = search_opcode(token_table[i]->operator)) > -1)		//opcode가 있을때, 이때만 pc값 기록, index로 테이블에 접근한다
		{
			token_table[i]->hasobjectcode = 1;							//object코드를 생성하는 토큰임을 기록
			if (!strcmp(inst_table[index]->type, "1"))	// format 1 일 때
			{
				locctr = locctr + 1;
				token_table[i]->pcValue = locctr;
			}
			else if (!strcmp(inst_table[index]->type, "2")) // format 2 일 때
			{
				locctr = locctr + 2;
				token_table[i]->pcValue = locctr;
			}
			else if (!strcmp(inst_table[index]->type, "3/4")) // format 3/4 일 때 +여부로 locctr 증가되는 수 정해줌
			{
				if (token_table[i]->operator[0] == '+')
					locctr = locctr + 4;
				else
					locctr = locctr + 3;

				token_table[i]->pcValue = locctr;
			}
		}
		else						// opcode가 없는 directive 일 때
		{
			if (!strcmp(token_table[i]->operator,"WORD"))
			{
				token_table[i]->hasobjectcode = 1;
				locctr = locctr + 3;
			}
			else if (!strcmp(token_table[i]->operator,"RESW"))
			{
				locctr = locctr + 3 * atoi(token_table[i]->total_operand);
			}
			else if (!strcmp(token_table[i]->operator,"RESB"))
			{
				locctr = locctr + atoi(token_table[i]->total_operand);
			}
			else if (!strcmp(token_table[i]->operator,"BYTE"))	//첫 글자가 X(16진수, 문자 2개당 1바이트), C(문자 1개당 1바이트) 일때 처리
			{
				token_table[i]->hasobjectcode = 1;

				strcpy(buff, token_table[i]->total_operand);
				char * tok = strtok(buff, "'");
				if (!strcmp(tok, "C"))
				{
					tok = strtok(NULL, "'");
					locctr = locctr + strlen(tok);
				}
				else if (!strcmp(tok, "X"))
				{
					tok = strtok(NULL, "'");
					locctr = locctr + strlen(tok) / 2;
				}
			}
			else if (!strcmp(token_table[i]->operator,"LTORG"))
			{
				for (int j = ltorg_line; j < i; j++)
				{
					if (token_table[j]->operand[0][0] == '=')
					{
						if (manage_literal(token_table[j]->operand[0]) < 0)		//이미 있는 리터럴을 무시
							continue;

						strcpy(buff, token_table[j]->operand[0]);
						char * tok = strtok(buff, "'");
						if (!strcmp(tok, "=C"))
						{
							tok = strtok(NULL, "'");
							locctr = locctr + strlen(tok);
							continue;
						}
						else if (!strcmp(tok, "=X"))
						{
							tok = strtok(NULL, "'");
							locctr = locctr + strlen(tok) / 2;
							continue;
						}

						strcpy(buff, token_table[j]->operand[0]);
						for (int z = 0; z < strlen(buff); z++)
						{
							if (buff[z] == '\'')
								break;
						}
						locctr = locctr + 3;
						
					}
				}

				ltorg_line = i;					//ltorg 라인 관리. 이전 ltorg에서 나온 리터럴은 무시하도록 해야하기때문
			}
			else if (!strcmp(token_table[i]->operator,"END"))
			{
				for (int j = ltorg_line; j < i; j++)
				{
					if (token_table[j]->operand[0][0] == '=')
					{
						if (manage_literal(token_table[j]->operand[0]) < 0)		//이미 있는 리터럴을 무시
							continue;

						strcpy(buff, token_table[j]->operand[0]);
						char * tok = strtok(buff, "'");
						if (!strcmp(tok, "=C"))
						{
							tok = strtok(NULL, "'");
							locctr = locctr + strlen(tok);
						}
						else if (!strcmp(tok, "=X"))
						{
							tok = strtok(NULL, "'");
							locctr = locctr + strlen(tok) / 2;
						}
					}
				}
				section_length[section_num] = locctr;

				ltorg_line = i;					//ltorg 라인 관리. 이전 ltorg에서 나온 리터럴은 무시하도록 해야하기때문
			}
		}
	}
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
static int assem_pass2(void)
{
	/* add your code here */
	int index = 0;
	section_num = 0;
	int ltorg_line = 0;

	for (int i = 0; i <= line_num; i++)
	{
		if (!strcmp(token_table[i]->operator,"START"))		//START 면 LOCCTR 을 그 인자로 초기화
		{
			locctr = strtol(token_table[i]->operand, NULL, 16);
			printf("%04X	%s	%s	%s	%s\n", locctr, token_table[i]->label, token_table[i]->operator,token_table[i]->total_operand, token_table[i]->comment);
			continue;
		}
		else if (!strcmp(token_table[i]->label, "."))		//주석 처리, 주석은 opcode를 찾을 필요가 없음.
		{
			printf("%s	%s	%s	%s\n", token_table[i]->label, token_table[i]->operator,token_table[i]->total_operand, token_table[i]->comment);
			continue;
		}


		if (!strcmp(token_table[i]->operator,"CSECT"))	//Control section이 바뀜 -> locctr 초기화, section_num으로 배열 내에서 구분
		{
			locctr = 0;
			section_num++;
		}

		if ((index = search_opcode(token_table[i]->operator)) > -1)		//opcode가 있을때, 이때만 pc값 기록
		{
			if (!strcmp(inst_table[index]->type, "1"))	// format 1 일 때
			{
				token_table[i]->object = strtol(inst_table[index]->opcode, NULL, 16);
				printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i]->label, token_table[i]->operator,token_table[i]->total_operand, token_table[i]->object);
				locctr = locctr + 1;
			}
			else if (!strcmp(inst_table[index]->type, "2")) // format 2 일 때 operand로 오는 레지스터에 따라 구분
			{
				token_table[i]->object = strtol(inst_table[index]->opcode, NULL, 16);

				for (int j = 0; j < 2; j++)
				{
					token_table[i]->object = token_table[i]->object << 4;			//주소계산. opcode를 쓴 후 4비트씩 밀면서 operand들 기록

					if (!strcmp(token_table[i]->operand[j], ""))
						token_table[i]->object += 0;
					else
					{
						if (!strcmp(token_table[i]->operand[j], "A"))
							token_table[i]->object += 0;
						else if (!strcmp(token_table[i]->operand[j], "X"))
							token_table[i]->object += 1;
						else if (!strcmp(token_table[i]->operand[j], "L"))
							token_table[i]->object += 2;
						else if (!strcmp(token_table[i]->operand[j], "B"))
							token_table[i]->object += 3;
						else if (!strcmp(token_table[i]->operand[j], "S"))
							token_table[i]->object += 4;
						else if (!strcmp(token_table[i]->operand[j], "T"))
							token_table[i]->object += 5;
						else if (!strcmp(token_table[i]->operand[j], "F"))
							token_table[i]->object += 6;
						else if (!strcmp(token_table[i]->operand[j], "PC"))
							token_table[i]->object += 8;
						else if (!strcmp(token_table[i]->operand[j], "SW"))
							token_table[i]->object += 9;
					}
				}
				printf("%04X	%s	%s	%s	%04X\n", locctr, token_table[i]->label, token_table[i]->operator,token_table[i]->total_operand, token_table[i]->object);
				locctr = locctr + 2;
			}
			else if (!strcmp(inst_table[index]->type, "3/4"))
			{
				int target_addr = -1;
				int dest = 0;

				if (token_table[i]->operator[0] == '+')				//4형식
				{
					token_table[i]->nixbpe += N + I + E;					//NIXBPE중에 4형식은 NIE 기본, X가 추가될수 있으므로 값 할당
					if (!strcmp(token_table[i]->operand[1], "X"))
						token_table[i]->nixbpe += X;

					target_addr = search_symbol(token_table[i]->operand[0]);
					if (target_addr == -2)							//라벨이 있으나 다른 섹션에 있음. M레코드에 써야하므로 구조체에 기록
					{
						mod_table[modification_line].index = i;
						mod_table[modification_line].section = section_num;
						modification_line++;
						target_addr = 0;
					}
					token_table[i]->object = strtol(inst_table[index]->opcode, NULL, 16) + (token_table[i]->nixbpe >> 4);		//nixpbe의 상위 2비트만 사용
					token_table[i]->object = (token_table[i]->object << 4) + (token_table[i]->nixbpe & 15);						//15와 & 연산 ->하위 4비트 만 사용
					token_table[i]->object = (token_table[i]->object << 20) + target_addr;										//앞의 (16진수로)3자리 계산후 5자리 뒤로 밈 그후 타겟주소 계산
					printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i]->label, token_table[i]->operator,token_table[i]->total_operand, token_table[i]->object);

					locctr = locctr + 4;
				}
				else												//3형식
				{
					if (token_table[i]->operand[0][0] == '#')					//immediate	addressing
					{
						char * temp = &token_table[i]->operand[0][1];
						target_addr = strtol(temp, NULL, 10);					//immediate은 10진수 이므로

						token_table[i]->nixbpe += I;							// I  비트만 사용

						token_table[i]->object = strtol(inst_table[index]->opcode, NULL, 16) + (token_table[i]->nixbpe >> 4);		//nixpbe의 상위 2비트만 사용
						token_table[i]->object = (token_table[i]->object << 4) + (token_table[i]->nixbpe & 15);						//15와 & 연산 ->하위 4비트 만 사용
						token_table[i]->object = (token_table[i]->object << 12) + target_addr;										//앞의 (16진수로)3자리 계산후 3자리 뒤로 밈 그후 타겟주소 계산
						printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i]->label, token_table[i]->operator,token_table[i]->total_operand, token_table[i]->object);

						locctr = locctr + 3;						
						continue;
					}
					else if (token_table[i]->operand[0][0] == '@')								//indirect addressing
					{
						char * temp = &token_table[i]->operand[0][1];
						target_addr = search_symbol(temp);
						dest = target_addr - token_table[i]->pcValue;							//target addr - pc 값

						token_table[i]->nixbpe += N + P;

						token_table[i]->object = strtol(inst_table[index]->opcode, NULL, 16) + (token_table[i]->nixbpe >> 4);		//nixpbe의 상위 2비트만 사용
						token_table[i]->object = (token_table[i]->object << 4) + (token_table[i]->nixbpe & 15);						//15와 & 연산 ->하위 4비트 만 사용
						token_table[i]->object = (token_table[i]->object << 12) + dest;												//앞의 (16진수로)3자리 계산후 3자리 뒤로 밈 그후 dest 주소 계산
						printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i]->label, token_table[i]->operator,token_table[i]->total_operand, token_table[i]->object);

						locctr = locctr + 3;
					}
					else																		//direct addressing
					{
						if (token_table[i]->operand[0][0] == '=')						//리터럴
						{
							target_addr = search_literal(token_table[i]->operand[0]);
						}
						else
						{
							if (inst_table[index]->operandAmount == 0)					//operand가 없을 때
							{
								target_addr = 0;										//RSUB 같은 operand가 없는 라인은 주소를 0으로 해줌

								token_table[i]->nixbpe += N + I;

								token_table[i]->object = strtol(inst_table[index]->opcode, NULL, 16) + (token_table[i]->nixbpe >> 4);
								token_table[i]->object = (token_table[i]->object << 4) + (token_table[i]->nixbpe & 15);		//15와 & 연산 ->하위 4비트 만 사용
								token_table[i]->object = (token_table[i]->object << 12) + target_addr;
								printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i]->label, token_table[i]->operator,token_table[i]->total_operand, token_table[i]->object);

								locctr = locctr + 3;
								continue;
							}
							else														//operand가 있을 때
								target_addr = search_symbol(token_table[i]->operand[0]);
						}


						if (target_addr == -1)	//에러 발생 symbol을 쓰려는데 없음.
							return -1;
						else	//타겟 주소를 받아옴.
						{
							dest = target_addr - token_table[i]->pcValue;
							if (-2048 <= dest && dest <= 2047)			//PC relative 방식 범위 내에 존재해야함
							{
								token_table[i]->nixbpe += N + I + P;

								token_table[i]->object = strtol(inst_table[index]->opcode, NULL, 16) + (token_table[i]->nixbpe >> 4);
								token_table[i]->object = (token_table[i]->object << 4) + (token_table[i]->nixbpe & 15);		//15와 & 연산 ->하위 4비트 만 사용
								if (dest < 0)
								{
									token_table[i]->object = (token_table[i]->object << 12) + (dest & 0x00000FFF);		//음수일 경우 하위 3바이트만 주소로 사용해야하므로 비트연산함
								}
								else
									token_table[i]->object = (token_table[i]->object << 12) + dest;
								printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i]->label, token_table[i]->operator,token_table[i]->total_operand, token_table[i]->object);

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
			if (!strcmp(token_table[i]->operator,"WORD"))
			{
				int check = 0;
				for (int j = 0; j < strlen(token_table[i]->operand[0]); j++)
				{
					if (token_table[i]->operand[0][j] == '-')						//- 가 있을떄,
					{
						char * tok = strtok(token_table[i]->operand[0], "-");
						int sym_index = search_symbol_index(tok);
						tok = strtok(NULL, "-\n");
						int sym_index2 = search_symbol_index(tok);
						sym_table[sym_index2].sign = '-';

						if (sym_table[sym_index].section != section_num || sym_table[sym_index2].section != section_num)
						{
							mod_table[modification_line].index = i;
							mod_table[modification_line].section = section_num;
							modification_line++;

							token_table[i]->object = 0;
							printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i]->label, token_table[i]->operator,token_table[i]->total_operand, token_table[i]->object);

							locctr = locctr + 3;
							check = 1;
							break;
						}
					}
					else if (token_table[i]->operand[0][j] == '+')					// + 가 있을때
					{
						char * tok = strtok(token_table[i]->operand[0], "+");
						int sym_index = search_symbol_index(tok);
						tok = strtok(NULL, "+\n");
						int sym_index2 = search_symbol_index(tok);

						if (sym_table[sym_index].section != section_num || sym_table[sym_index2].section != section_num)
						{
							mod_table[modification_line].index = i;
							mod_table[modification_line].section = section_num;
							modification_line++;

							token_table[i]->object = 0;
							printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i]->label, token_table[i]->operator,token_table[i]->total_operand, token_table[i]->object);

							locctr = locctr + 3;
							check = 1;
							break;
						}
					}
				}
				if (check == 0)														// 두 부호가 존재하지 않을때, 즉 계산하지 않아도될때
				{
					token_table[i]->object = 3 * atoi(token_table[i]->operand[0]);
					printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i]->label, token_table[i]->operator,token_table[i]->total_operand, token_table[i]->object);

					locctr = locctr + 3;
				}
			}
			else if (!strcmp(token_table[i]->operator,"BYTE"))	//첫 글자가 X(16진수, 문자 2개당 1바이트), C(문자 1개당 1바이트) 일때 처리
			{
				strcpy(buff, token_table[i]->total_operand);
				char * tok = strtok(buff, "'");
				if (!strcmp(tok, "C"))
				{
					tok = strtok(NULL, "'");
					for (int j = 0; j < strlen(tok); j++)
					{
						token_table[i]->object += tok[j];
						if (j == strlen(tok) - 1)
							continue;
						token_table[i]->object = token_table[i]->object << 8;					//바이트 수 만큼 object 코드 쉬프트함
					}
					printf("%04X	%s	%s	%s	%X\n", locctr, token_table[i]->label, token_table[i]->operator,token_table[i]->total_operand, token_table[i]->object);
					locctr = locctr + strlen(tok);
				}
				else if (!strcmp(tok, "X"))
				{
					tok = strtok(NULL, "'");
					token_table[i]->object = strtol(tok, NULL, 16);
					printf("%04X	%s	%s	%s	%02X\n", locctr, token_table[i]->label, token_table[i]->operator,token_table[i]->total_operand, token_table[i]->object);
					locctr = locctr + strlen(tok) / 2;
				}
			}
			else if (!strcmp(token_table[i]->operator,"RESW"))
			{
				printf("%04X	%s	%s	%s\n", locctr, token_table[i]->label, token_table[i]->operator,token_table[i]->total_operand);
				locctr = locctr + 3 * atoi(token_table[i]->total_operand);
			}
			else if (!strcmp(token_table[i]->operator,"RESB"))
			{
				printf("%04X	%s	%s	%s\n", locctr, token_table[i]->label, token_table[i]->operator,token_table[i]->total_operand);
				locctr = locctr + atoi(token_table[i]->total_operand);
			}
			else if (!strcmp(token_table[i]->operator,"LTORG") || !strcmp(token_table[i]->operator,"END"))
			{
				printf("	%s	%s	%s\n", token_table[i]->label, token_table[i]->operator,token_table[i]->total_operand);
				for (int j = ltorg_line; j < i; j++)
				{
					if (token_table[j]->operand[0][0] == '=')
					{
						int lit_index = search_literal_index(token_table[j]->operand[0]);
						if (lit_table[lit_index].object != 0)				//이미 기록되어 있다면. 무시
							;
						else
						{
							strcpy(buff, token_table[j]->operand[0]);
							char * tok = strtok(buff, "'");

							if (!strcmp(tok, "=C"))
							{
								tok = strtok(NULL, "'");
								for (int j = 0; j < strlen(tok); j++)
								{
									lit_table[lit_index].object += tok[j];
									if (j == strlen(tok) - 1)
										continue;
									lit_table[lit_index].object = lit_table[lit_index].object << 8;
								}
								printf("%04X	%s	%s		%X\n", locctr, "*", lit_table[lit_index].literal, lit_table[lit_index].object);
								locctr = locctr + strlen(tok);
								continue;
							}
							else if (!strcmp(tok, "=X"))
							{
								tok = strtok(NULL, "'");
								lit_table[lit_index].object = strtol(tok, NULL, 16);

								printf("%04X	%s	%s		%02X\n", locctr, "*", lit_table[lit_index].literal, lit_table[lit_index].object);
								locctr = locctr + strlen(tok) / 2;
								continue;
							}
							
							strcpy(buff, token_table[j]->operand[0]);
							char * tok2 = strtok(buff, "=");
							lit_table[lit_index].object = strtol(tok2, NULL, 16);
							lit_table[lit_index].litcheck = -1;
							printf("%04X	%s	%s		%06X\n", locctr, "*", lit_table[lit_index].literal, lit_table[lit_index].object);
							locctr = locctr + 3;
							
						}
					}
				}
				ltorg_line = i;					//ltorg 라인 관리. 이전 ltorg에서 나온 리터럴은 무시하도록 해야하기때문
			}
			else
				printf("	%s	%s	%s	%s\n", token_table[i]->label, token_table[i]->operator,token_table[i]->total_operand, token_table[i]->comment);

		}
	}
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
void make_objectcode_output(char *file_name)
{
	/* add your code here */
	FILE * file;
	if (file_name == NULL)
		file = stdout;
	else
		file = fopen(file_name, "w");



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

		if (!strcmp(token_table[i]->operator,"START"))
		{
			locctr = strtol(token_table[i]->operand, NULL, 16);
			firstaddr = locctr;												//H레코드 에서 시작주소를 저장하기 위해 저장
			totalbyte = strtol(token_table[i]->operand, NULL, 16);
			fprintf(file, "H%-6s%06X%06X\n", token_table[i]->label, totalbyte, section_length[section_num]);		//H레코드 기록
		}

		if ((index = search_opcode(token_table[i]->operator)) > -1)		//opcode가 있을때 각 토큰들에선 현재 얼마까지 읽었는지만 기록
		{
			if (!strcmp(inst_table[index]->type, "1"))	// format 1 일 때
			{
				count = 1;
				totalbyte = totalbyte + count;
				locctr = locctr + 1;
			}
			else if (!strcmp(inst_table[index]->type, "2")) // format 2 일 때 operand로 오는 레지스터에 따라 구분
			{
				count = 2;
				totalbyte = totalbyte + count;
				locctr = locctr + 2;
			}
			else if (!strcmp(inst_table[index]->type, "3/4"))
			{
				if (token_table[i]->operator[0] == '+')
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
			if (!strcmp(token_table[i]->operator,"WORD"))
			{
				count = 3;
				totalbyte = totalbyte + count;
				locctr = locctr + 3;
			}
			else if (!strcmp(token_table[i]->operator,"BYTE"))	//첫 글자가 X(16진수, 문자 2개당 1바이트), C(문자 1개당 1바이트) 일때 처리
			{
				strcpy(buff, token_table[i]->total_operand);
				char * tok = strtok(buff, "'");
				if (!strcmp(tok, "C"))
				{
					tok = strtok(NULL, "'");
					count = strlen(tok);

					totalbyte = totalbyte + count;
					locctr = locctr + count;
				}
				else if (!strcmp(tok, "X"))
				{
					tok = strtok(NULL, "'");
					count = strlen(tok) / 2;
					totalbyte = totalbyte + count;
					locctr = locctr + count;
				}
			}
			else if (!strcmp(token_table[i]->operator,"RESW"))
			{
				count = 3 * atoi(token_table[i]->total_operand);
				locctr = locctr + count;

			}
			else if (!strcmp(token_table[i]->operator,"RESB"))
			{
				count = atoi(token_table[i]->total_operand);
				locctr = locctr + count;
			}
			else if (!strcmp(token_table[i]->operator,"EXTDEF"))
			{
				count = 0;
				fprintf(file, "D");

				for (int j = 0; j < MAX_OPERAND; j++)
				{
					if (!strcmp(token_table[i]->operand[j], ""))
						break;
					else
					{
						int addr = search_symbol(token_table[i]->operand[j]);				//각 주소를 비교 문자열로 받아옴
						fprintf(file, "%-6s%06X", token_table[i]->operand[j], addr);
					}
				}
				fprintf(file, "\n");
			}
			else if (!strcmp(token_table[i]->operator,"EXTREF"))
			{
				count = 0;
				fprintf(file, "R");

				for (int j = 0; j < MAX_OPERAND; j++)
				{
					if (!strcmp(token_table[i]->operand[j], ""))
						break;
					else
						fprintf(file, "%-6s", token_table[i]->operand[j]);
				}
				fprintf(file, "\n");
			}
			else if (!strcmp(token_table[i]->operator,"LTORG"))			//LTORG 위 라인 까지 object 코드로 출력, 그 후 리터럴에 대한 코드 정보 처리(리터럴은 CSECT,END에서 출력)
			{
				count = 0;
				fprintf(file, "T%06X%02X", current_addr, totalbyte - count);				
				for (int j = checkindex; j < i; j++)											
				{
					int temp_index = search_opcode(token_table[j]->operator);
					if (token_table[j]->hasobjectcode == 1)
					{
						if (temp_index == -1)
						{
							if (!strcmp(token_table[j]->operator,"WORD"))
								fprintf(file, "%06X", token_table[j]->object);
							else
								fprintf(file, "%02X", token_table[j]->object);
						}
						else if (!strcmp(inst_table[temp_index]->type, "2"))
							fprintf(file, "%04X", token_table[j]->object);
						else
							fprintf(file, "%06X", token_table[j]->object);
					}
				}
				fprintf(file, "\n");

				current_addr += totalbyte - count;
				totalbyte = 0;
				checkindex = i;		//어디까지 읽은지 저장

				/////////////////////리터럴 코드

				current_addr = locctr;
				for (int j = 0; j <= literal_line; j++)
				{
					if (lit_table[j].section == section_num)
					{
						strcpy(buff, lit_table[j].literal);
						char * tok = strtok(buff, "'");

						if (!strcmp(tok, "=C"))
						{
							tok = strtok(NULL, "'");

							count = strlen(tok);
							totalbyte = totalbyte + count;
							locctr = locctr + count;
							continue;
						}
						else if (!strcmp(tok, "=X"))
						{
							tok = strtok(NULL, "'");
							count = strlen(tok) / 2;
							totalbyte = totalbyte + count;
							locctr = locctr + count;
							continue;
						}

						count = 3;
						totalbyte = totalbyte + count;
						locctr = locctr + count;

					}
				}
			}
			else if (!strcmp(token_table[i]->operator,"END"))
			{
				
				for (int j = 0; j <= literal_line; j++)
				{
					if (lit_table[j].section == section_num)
					{
						strcpy(buff, lit_table[j].literal);
						char * tok = strtok(buff, "'");

						if (!strcmp(tok, "=C"))
						{
							tok = strtok(NULL, "'");

							count = strlen(tok);
							totalbyte = totalbyte + count;
							locctr = locctr + count;

						}
						else if (!strcmp(tok, "=X"))
						{
							tok = strtok(NULL, "'");
							count = strlen(tok) / 2;
							totalbyte = totalbyte + count;
							locctr = locctr + count;

						}
					}
				}
				fprintf(file, "T%06X%02X", current_addr, totalbyte);			

				for (int j = checkindex; j < i; j++)											
				{
					int temp_index = search_opcode(token_table[j]->operator);
					if (token_table[j]->hasobjectcode == 1)
					{

						if (temp_index == -1)
						{
							if (!strcmp(token_table[j]->operator,"WORD"))
								fprintf(file, "%06X", token_table[j]->object);
							else
								fprintf(file, "%02X", token_table[j]->object);
						}
						else if (!strcmp(inst_table[temp_index]->type, "2"))
							fprintf(file, "%04X", token_table[j]->object);
						else
							fprintf(file, "%06X", token_table[j]->object);
					}
				}
				/////////////////////리터럴 코드 출력

				for (int j = 0; j <= literal_line; j++)
					if (lit_table[j].section == section_num)
					{
						if(lit_table[j].litcheck == -1)
							fprintf(file, "%06X\n", lit_table[j].object);
						else
							fprintf(file, "%02X\n", lit_table[j].object);
					}


				for (int j = 0; j < modification_line; j++)						//modification record 가 있을때 출력
				{
					int x = mod_table[j].index;
					if (mod_table[j].section == section_num)
					{
						if (!strcmp(token_table[x]->operator,"WORD"))
						{
							int check = 0;
							for (int k = 0; k < strlen(token_table[x]->total_operand); k++)
							{
								if (token_table[x]->total_operand[k] == '-')						//- 가 있을떄,
								{
									char * tok = strtok(token_table[x]->total_operand, "-");
									int sym_index = search_symbol_index(tok);
									tok = strtok(NULL, "-\n");
									int sym_index2 = search_symbol_index(tok);
									fprintf(file, "M%06X06%c%s\n", token_table[x]->current_locctr, sym_table[sym_index].sign, sym_table[sym_index].symbol);
									fprintf(file, "M%06X06%c%s\n", token_table[x]->current_locctr, sym_table[sym_index2].sign, sym_table[sym_index2].symbol);
									break;
								}
							}
						}
						else
							fprintf(file, "M%06X05+%s\n", token_table[x]->current_locctr + 1, token_table[x]->operand[0]);
					}
				}

				if (section_num == 0)
					fprintf(file, "E%06X\n", firstaddr);
				else
					fprintf(file, "E\n");

			}
			else if (!strcmp(token_table[i]->operator,"CSECT"))	//Control section이 바뀜 -> locctr 초기화, section_num으로 배열 내에서 구분
			{
				if (hasliteral[section_num]==1)			//리터럴이 있을때 출력
				{
					fprintf(file, "T%06X%02X", current_addr, totalbyte);

					for (int j = 0; j <= literal_line; j++)
					{
						if (lit_table[j].section == section_num)
						{
							if (lit_table[j].litcheck == -1)
								fprintf(file, "%06X", lit_table[j].object);
							else
								fprintf(file, "%02X", lit_table[j].object);
						}
					}

		
					fprintf(file, "\n");
				}
				else
				{
					count = 0;
					fprintf(file, "T%06X%02X", current_addr, totalbyte - count);				//섹션이 바뀔때 다 출력하지 못한것들 출력

					for (int j = checkindex; j < i; j++)											
					{
						int temp_index = search_opcode(token_table[j]->operator);
						if (token_table[j]->hasobjectcode == 1)
						{
							
							if (temp_index == -1)
							{
								if (!strcmp(token_table[j]->operator,"WORD"))
									fprintf(file, "%06X", token_table[j]->object);
								else
									fprintf(file, "%02X", token_table[j]->object);
							}
							else if (!strcmp(inst_table[temp_index]->type, "2"))
								fprintf(file, "%04X", token_table[j]->object);
							else
								fprintf(file, "%06X", token_table[j]->object);
						}
					}
					fprintf(file, "\n");

				}

				for (int j = 0; j < modification_line; j++)
				{
					int x = mod_table[j].index;
					if (mod_table[j].section == section_num)
					{
						if (!strcmp(token_table[x]->operator,"WORD"))
						{
							int check = 0;
							for (int k = 0; k < strlen(token_table[x]->total_operand); k++)
							{
								if (token_table[x]->total_operand[k] == '-')						//- 가 있을떄,
								{
									char * tok = strtok(token_table[x]->total_operand, "-");
									int sym_index = search_symbol_index(tok);
									tok = strtok(NULL, "-\n");
									int sym_index2 = search_symbol_index(tok);

									fprintf(file, "M%06X06%c%s\n", token_table[x]->current_locctr, sym_table[sym_index].sign, sym_table[sym_index].symbol);

									fprintf(file, "M%06X06%c%s\n", token_table[x]->current_locctr, sym_table[sym_index2].sign, sym_table[sym_index2].symbol);

									break;

								}
							}
						}
						else
							fprintf(file, "M%06X05+%s\n", token_table[x]->current_locctr + 1, token_table[x]->operand[0]);

					}
				}

				if (section_num == 0)
					fprintf(file, "E%06X\n", firstaddr);
				else
					fprintf(file, "E\n");
				

				locctr = 0;
				section_num++;
				totalbyte = 0;
				checkindex = i;
				current_addr = 0;
				fprintf(file, "\nH%-6s%06X%06X\n", token_table[i]->label, locctr, section_length[section_num]);

			}
		}

		if (totalbyte >= 30)
		{
			fprintf(file, "T%06X%02X", current_addr, totalbyte - count);					//시작주소, 현재까지 파일에 쓴 바이트
			for (int j = checkindex; j < i; j++)											//30바이트가 넘기전까지 출력
			{
				int temp_index = search_opcode(token_table[j]->operator);
				if (token_table[j]->hasobjectcode == 1)
				{
					if (temp_index == -1)
					{
						if (!strcmp(token_table[j]->operator,"WORD"))
							fprintf(file, "%06X", token_table[j]->object);
						else
							fprintf(file, "%02X", token_table[j]->object);
					}
					else if (!strcmp(inst_table[temp_index]->type, "2"))
						fprintf(file, "%04X", token_table[j]->object);
					else
						fprintf(file, "%06X", token_table[j]->object);
				}
			}
			fprintf(file, "\n");

			current_addr += totalbyte - count;
			totalbyte = count;
			checkindex = i;
		}
	}
	fclose(file);
}
