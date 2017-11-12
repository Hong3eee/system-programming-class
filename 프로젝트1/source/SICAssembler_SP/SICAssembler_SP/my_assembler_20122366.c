/*
* ȭ�ϸ� : my_assembler.c
* ��  �� : �� ���α׷��� SIC/XE �ӽ��� ���� ������ Assembler ���α׷��� ���η�ƾ����,
* �Էµ� ������ �ڵ� ��, ��ɾ �ش��ϴ� OPCODE�� ã�� ����Ѵ�.
*
*/

/*
*
* ���α׷��� ����� �����Ѵ�.
*
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>

#include "my_assembler_20122366.h"

/* ----------------------------------------------------------------------------------
* ���� : ����ڷ� ���� ����� ������ �޾Ƽ� ��ɾ��� OPCODE�� ã�� ����Ѵ�.
* �Ű� : ���� ����, ����� ����
* ��ȯ : ���� = 0, ���� = < 0
* ���� : ���� ����� ���α׷��� ����Ʈ ������ �����ϴ� ��ƾ�� ������ �ʾҴ�.
*		   ���� �߰������� �������� �ʴ´�.
* ----------------------------------------------------------------------------------
*/

char * buff;		// inst_table�� ���鶧 ����ϴ� ����


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
* ���� : ���α׷� �ʱ�ȭ�� ���� �ڷᱸ�� ���� �� ������ �д� �Լ��̴�.
* �Ű� : ����
* ��ȯ : �������� = 0 , ���� �߻� = -1
* ���� : ������ ��ɾ� ���̺��� ���ο� �������� �ʰ� ������ �����ϰ� �ϱ�
*		   ���ؼ� ���� ������ �����Ͽ� ���α׷� �ʱ�ȭ�� ���� ������ �о� �� �� �ֵ���
*		   �����Ͽ���.
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

	while (!feof(file))				//������ ������
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

		//printf("%s	%s	%s	%d\n", inst_table[i]->operator, inst_table[i]->type,inst_table[i]->opcode, inst_table[i]->operandAmount); //�ֿܼ� Ȯ��

		inst_index = i;			//0���� ���� ����

		i++;
	}

	buff = (char *)malloc(1024); // �޸� ������ ���� ���� �Ҵ��ؼ� ħ�� ����......

	fclose(file);


	return errno;
}

/* ----------------------------------------------------------------------------------
* ���� : ����� �� �ҽ��ڵ带 �о���� �Լ��̴�.
* �Ű� : ������� �ҽ����ϸ�
* ��ȯ : �������� = 0 , ���� < 0
* ���� :
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
* ���� : �ҽ� �ڵ带 �о�� ��ū������ �м��ϰ� ��ū ���̺��� �ۼ��ϴ� �Լ��̴�.
*        �н� 1�� ���� ȣ��ȴ�.
* �Ű� : �ҽ��ڵ��� ���ι�ȣ
* ��ȯ : �������� = 0 , ���� < 0
* ���� : my_assembler ���α׷������� ���δ����� ��ū �� ������Ʈ ������ �ϰ� �ִ�.
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
	if (first == '\t')				//label�� ���� ��츦 ó��
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

	tok2 = strtok(token_table[index]->operand[0], ",\t");				// ',','\t' ���� �����ؼ� operand�� �� index ��ġ�� �Ҵ�

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

	token_table[index]->nixbpe = 0; //bit�� 0���� �ʱ�ȭ
	token_table[index]->object = 0;
	token_table[index]->hasobjectcode = 0;			//object�ڵ带 ����� �������� Ȯ�� 0 -> �ȸ��� 1 -> ����
	token_line = index;		//0���� ����

}

/* ----------------------------------------------------------------------------------
* ���� : �Է� ���ڿ��� ���� �ڵ������� �˻��ϴ� �Լ��̴�.
* �Ű� : ��ū ������ ���е� ���ڿ�
* ��ȯ : �������� = ���� ���̺� �ε���, ���� < 0
* ���� :
*
* ----------------------------------------------------------------------------------
*/
int search_opcode(char *str)	//str�� �񱳰����� �ؼ� inst_table�� ��� ������ index�� ã��. ������ -1 ����
{
	/* add your code here */
	char compare[100];

	if (str[0] == '+')					//4���� ó�� +�� ����
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
* ���� : �Էµ� ���ڿ��� �̸��� ���� ���Ͽ� ���α׷��� ����� �����ϴ� �Լ��̴�.
*        ���⼭ ��µǴ� ������ ��ɾ� ���� OPCODE�� ��ϵ� ǥ(���� 4��) �̴�.
* �Ű� : ������ ������Ʈ ���ϸ�
* ��ȯ : ����
* ���� : ���� ���ڷ� NULL���� ���´ٸ� ���α׷��� ����� ǥ��������� ������
*        ȭ�鿡 ������ش�.
*        ���� ���� 4�������� ���̴� �Լ��̹Ƿ� ������ ������Ʈ������ ������ �ʴ´�.
* -----------------------------------------------------------------------------------
*/
void make_opcode_output(char *file_name)
{
	/* add your code here */
	int index = 0;
	FILE * file = fopen(file_name, "w");

	for (int i = 0; i <= token_line; i++)
	{

		if (!strcmp(token_table[i]->label, "."))		//�ּ� ó��, �ּ��� opcode�� ã�� �ʿ䰡 ����.
		{
			fprintf(file, "%s	%s	%s	%s\n", token_table[i]->label, token_table[i]->operator,token_table[i]->operand[0], token_table[i]->comment);
			continue;
		}

		if ((index = search_opcode(token_table[i]->operator)) > -1)		//opcode�� ������
		{
			if (inst_table[index]->operandAmount == 0)		// format 1 �� ��
				fprintf(file, "%s	%s	%s	%s\n", token_table[i]->label, token_table[i]->operator,"", inst_table[index]->opcode);

			else
				fprintf(file, "%s	%s	%s	%s\n", token_table[i]->label, token_table[i]->operator,token_table[i]->operand[0], inst_table[index]->opcode);
		}
		else						// opcode�� ���� directive �� ��
			fprintf(file, "%s	%s	%s\n", token_table[i]->label, token_table[i]->operator,token_table[i]->operand[0], token_table[i]->comment);

	}
	fclose(file);

}




/* --------------------------------------------------------------------------------*
* ------------------------- ���� ������Ʈ���� ����� �Լ� --------------------------*
* --------------------------------------------------------------------------------*/

/* ----------------------------------------------------------------------------------
* ���� : �ɺ����� ���Ͽ� ������ ���̺� �߰��ϰ� ������ �����߻�
* �Ű� : ���� symbol
* ��ȯ : symbol�� ��� �߰��� �� ���� = 0, �̹� symbol�� �����ϸ� -1
* -----------------------------------------------------------------------------------
*/
int manage_symbol(char *str)
{
	/* add your code here */
	for (int i = 0; i <= symbol_line; i++)
	{
		if ((!strcmp(str, sym_table[i].symbol)) && section_num == sym_table[i].section)	//symbol�� �����鼭 �� symbol�� ���������϶� 
			return -1;
	}
	
	//symbol�� ������ symtab �� �߰�, line �� ����
	strcpy(sym_table[symbol_line + 1].symbol, str);
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
int manage_literal(char *str)
{
	/* add your code here */
	for (int i = 0; i <= literal_line; i++)
	{
		if ((!strcmp(str, lit_table[i].literal)))// && section_num == lit_table[i].section)	//literal�� �����鼭 �� literal�� ���������϶� 
			return -1;
	}

	//literal�� ������ littab �� �߰�, line �� ����
	strcpy(lit_table[literal_line + 1].literal, str);
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
int search_symbol(char *str)
{
	int addr = -1;
	/* add your code here */
	for (int i = 0; i <= symbol_line; i++)
	{
		if ((!strcmp(str, sym_table[i].symbol)))
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
* ���� : �Է� ���ڿ��� ���ͷ� �ε����� ã�� �Լ��̴�.
* �Ű� : ���� ���ڿ�
* ��ȯ : �������� = ���ͷ� ���̺� �ε���, ���� < 0
* ----------------------------------------------------------------------------------
*/
int search_literal_index(char *str)	//str�� �񱳰����� �ؼ� inst_table�� ��� ������ index�� ã��. ������ -1 ����
{
	for (int i = 0; i <= literal_line; i++)
	{
		if (!strcmp(str, lit_table[i].literal))
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
int search_symbol_index(char *str)	//str�� �񱳰����� �ؼ� inst_table�� ��� ������ index�� ã��. ������ -1 ����
{
	for (int i = 0; i <= symbol_line; i++)
	{
		if (!strcmp(str, sym_table[i].symbol))
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
static int assem_pass1(void)
{
	/* add your code here */
	int index = 0;
	int ltorg_line = 0;
	for (int i = 0; i <= line_num; i++)
	{
		if (!strcmp(token_table[i]->operator,"START"))		//START �� LOCCTR �� �� ���ڷ� �ʱ�ȭ
			locctr = strtol(token_table[i]->operand, NULL, 16);
		else if (!strcmp(token_table[i]->label, "."))		//�ּ� ó��, �ּ��� opcode�� ã�� �ʿ䰡 ����.
			continue;


		token_table[i]->current_locctr = locctr;		//�� ��ū�� locctr ���

		if (!strcmp(token_table[i]->operator,"CSECT"))	//Control section�� �ٲ� -> locctr �ʱ�ȭ, section_num���� �迭 ������ ����
		{
			section_length[section_num] = locctr;			//�� ������ ���� �����ص�
			locctr = 0;
			section_num++;
			continue;
		}

		if (strcmp(token_table[i]->label, "")) // ���� ������	�ɺ� ó��
			if (manage_symbol(token_table[i]->label) < 0)
				return -1;										//���� ���� ���� ���� �̸��� ���� �����ϸ� ���� ���


		if ((index = search_opcode(token_table[i]->operator)) > -1)		//opcode�� ������, �̶��� pc�� ���, index�� ���̺� �����Ѵ�
		{
			token_table[i]->hasobjectcode = 1;							//object�ڵ带 �����ϴ� ��ū���� ���
			if (!strcmp(inst_table[index]->type, "1"))	// format 1 �� ��
			{
				locctr = locctr + 1;
				token_table[i]->pcValue = locctr;
			}
			else if (!strcmp(inst_table[index]->type, "2")) // format 2 �� ��
			{
				locctr = locctr + 2;
				token_table[i]->pcValue = locctr;
			}
			else if (!strcmp(inst_table[index]->type, "3/4")) // format 3/4 �� �� +���η� locctr �����Ǵ� �� ������
			{
				if (token_table[i]->operator[0] == '+')
					locctr = locctr + 4;
				else
					locctr = locctr + 3;

				token_table[i]->pcValue = locctr;
			}
		}
		else						// opcode�� ���� directive �� ��
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
			else if (!strcmp(token_table[i]->operator,"BYTE"))	//ù ���ڰ� X(16����, ���� 2���� 1����Ʈ), C(���� 1���� 1����Ʈ) �϶� ó��
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
						if (manage_literal(token_table[j]->operand[0]) < 0)		//�̹� �ִ� ���ͷ��� ����
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

				ltorg_line = i;					//ltorg ���� ����. ���� ltorg���� ���� ���ͷ��� �����ϵ��� �ؾ��ϱ⶧��
			}
			else if (!strcmp(token_table[i]->operator,"END"))
			{
				for (int j = ltorg_line; j < i; j++)
				{
					if (token_table[j]->operand[0][0] == '=')
					{
						if (manage_literal(token_table[j]->operand[0]) < 0)		//�̹� �ִ� ���ͷ��� ����
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

				ltorg_line = i;					//ltorg ���� ����. ���� ltorg���� ���� ���ͷ��� �����ϵ��� �ؾ��ϱ⶧��
			}
		}
	}
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
static int assem_pass2(void)
{
	/* add your code here */
	int index = 0;
	section_num = 0;
	int ltorg_line = 0;

	for (int i = 0; i <= line_num; i++)
	{
		if (!strcmp(token_table[i]->operator,"START"))		//START �� LOCCTR �� �� ���ڷ� �ʱ�ȭ
		{
			locctr = strtol(token_table[i]->operand, NULL, 16);
			printf("%04X	%s	%s	%s	%s\n", locctr, token_table[i]->label, token_table[i]->operator,token_table[i]->total_operand, token_table[i]->comment);
			continue;
		}
		else if (!strcmp(token_table[i]->label, "."))		//�ּ� ó��, �ּ��� opcode�� ã�� �ʿ䰡 ����.
		{
			printf("%s	%s	%s	%s\n", token_table[i]->label, token_table[i]->operator,token_table[i]->total_operand, token_table[i]->comment);
			continue;
		}


		if (!strcmp(token_table[i]->operator,"CSECT"))	//Control section�� �ٲ� -> locctr �ʱ�ȭ, section_num���� �迭 ������ ����
		{
			locctr = 0;
			section_num++;
		}

		if ((index = search_opcode(token_table[i]->operator)) > -1)		//opcode�� ������, �̶��� pc�� ���
		{
			if (!strcmp(inst_table[index]->type, "1"))	// format 1 �� ��
			{
				token_table[i]->object = strtol(inst_table[index]->opcode, NULL, 16);
				printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i]->label, token_table[i]->operator,token_table[i]->total_operand, token_table[i]->object);
				locctr = locctr + 1;
			}
			else if (!strcmp(inst_table[index]->type, "2")) // format 2 �� �� operand�� ���� �������Ϳ� ���� ����
			{
				token_table[i]->object = strtol(inst_table[index]->opcode, NULL, 16);

				for (int j = 0; j < 2; j++)
				{
					token_table[i]->object = token_table[i]->object << 4;			//�ּҰ��. opcode�� �� �� 4��Ʈ�� �и鼭 operand�� ���

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

				if (token_table[i]->operator[0] == '+')				//4����
				{
					token_table[i]->nixbpe += N + I + E;					//NIXBPE�߿� 4������ NIE �⺻, X�� �߰��ɼ� �����Ƿ� �� �Ҵ�
					if (!strcmp(token_table[i]->operand[1], "X"))
						token_table[i]->nixbpe += X;

					target_addr = search_symbol(token_table[i]->operand[0]);
					if (target_addr == -2)							//���� ������ �ٸ� ���ǿ� ����. M���ڵ忡 ����ϹǷ� ����ü�� ���
					{
						mod_table[modification_line].index = i;
						mod_table[modification_line].section = section_num;
						modification_line++;
						target_addr = 0;
					}
					token_table[i]->object = strtol(inst_table[index]->opcode, NULL, 16) + (token_table[i]->nixbpe >> 4);		//nixpbe�� ���� 2��Ʈ�� ���
					token_table[i]->object = (token_table[i]->object << 4) + (token_table[i]->nixbpe & 15);						//15�� & ���� ->���� 4��Ʈ �� ���
					token_table[i]->object = (token_table[i]->object << 20) + target_addr;										//���� (16������)3�ڸ� ����� 5�ڸ� �ڷ� �� ���� Ÿ���ּ� ���
					printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i]->label, token_table[i]->operator,token_table[i]->total_operand, token_table[i]->object);

					locctr = locctr + 4;
				}
				else												//3����
				{
					if (token_table[i]->operand[0][0] == '#')					//immediate	addressing
					{
						char * temp = &token_table[i]->operand[0][1];
						target_addr = strtol(temp, NULL, 10);					//immediate�� 10���� �̹Ƿ�

						token_table[i]->nixbpe += I;							// I  ��Ʈ�� ���

						token_table[i]->object = strtol(inst_table[index]->opcode, NULL, 16) + (token_table[i]->nixbpe >> 4);		//nixpbe�� ���� 2��Ʈ�� ���
						token_table[i]->object = (token_table[i]->object << 4) + (token_table[i]->nixbpe & 15);						//15�� & ���� ->���� 4��Ʈ �� ���
						token_table[i]->object = (token_table[i]->object << 12) + target_addr;										//���� (16������)3�ڸ� ����� 3�ڸ� �ڷ� �� ���� Ÿ���ּ� ���
						printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i]->label, token_table[i]->operator,token_table[i]->total_operand, token_table[i]->object);

						locctr = locctr + 3;						
						continue;
					}
					else if (token_table[i]->operand[0][0] == '@')								//indirect addressing
					{
						char * temp = &token_table[i]->operand[0][1];
						target_addr = search_symbol(temp);
						dest = target_addr - token_table[i]->pcValue;							//target addr - pc ��

						token_table[i]->nixbpe += N + P;

						token_table[i]->object = strtol(inst_table[index]->opcode, NULL, 16) + (token_table[i]->nixbpe >> 4);		//nixpbe�� ���� 2��Ʈ�� ���
						token_table[i]->object = (token_table[i]->object << 4) + (token_table[i]->nixbpe & 15);						//15�� & ���� ->���� 4��Ʈ �� ���
						token_table[i]->object = (token_table[i]->object << 12) + dest;												//���� (16������)3�ڸ� ����� 3�ڸ� �ڷ� �� ���� dest �ּ� ���
						printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i]->label, token_table[i]->operator,token_table[i]->total_operand, token_table[i]->object);

						locctr = locctr + 3;
					}
					else																		//direct addressing
					{
						if (token_table[i]->operand[0][0] == '=')						//���ͷ�
						{
							target_addr = search_literal(token_table[i]->operand[0]);
						}
						else
						{
							if (inst_table[index]->operandAmount == 0)					//operand�� ���� ��
							{
								target_addr = 0;										//RSUB ���� operand�� ���� ������ �ּҸ� 0���� ����

								token_table[i]->nixbpe += N + I;

								token_table[i]->object = strtol(inst_table[index]->opcode, NULL, 16) + (token_table[i]->nixbpe >> 4);
								token_table[i]->object = (token_table[i]->object << 4) + (token_table[i]->nixbpe & 15);		//15�� & ���� ->���� 4��Ʈ �� ���
								token_table[i]->object = (token_table[i]->object << 12) + target_addr;
								printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i]->label, token_table[i]->operator,token_table[i]->total_operand, token_table[i]->object);

								locctr = locctr + 3;
								continue;
							}
							else														//operand�� ���� ��
								target_addr = search_symbol(token_table[i]->operand[0]);
						}


						if (target_addr == -1)	//���� �߻� symbol�� �����µ� ����.
							return -1;
						else	//Ÿ�� �ּҸ� �޾ƿ�.
						{
							dest = target_addr - token_table[i]->pcValue;
							if (-2048 <= dest && dest <= 2047)			//PC relative ��� ���� ���� �����ؾ���
							{
								token_table[i]->nixbpe += N + I + P;

								token_table[i]->object = strtol(inst_table[index]->opcode, NULL, 16) + (token_table[i]->nixbpe >> 4);
								token_table[i]->object = (token_table[i]->object << 4) + (token_table[i]->nixbpe & 15);		//15�� & ���� ->���� 4��Ʈ �� ���
								if (dest < 0)
								{
									token_table[i]->object = (token_table[i]->object << 12) + (dest & 0x00000FFF);		//������ ��� ���� 3����Ʈ�� �ּҷ� ����ؾ��ϹǷ� ��Ʈ������
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
		else						// opcode�� ���� directive �� ��
		{
			if (!strcmp(token_table[i]->operator,"WORD"))
			{
				int check = 0;
				for (int j = 0; j < strlen(token_table[i]->operand[0]); j++)
				{
					if (token_table[i]->operand[0][j] == '-')						//- �� ������,
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
					else if (token_table[i]->operand[0][j] == '+')					// + �� ������
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
				if (check == 0)														// �� ��ȣ�� �������� ������, �� ������� �ʾƵ��ɶ�
				{
					token_table[i]->object = 3 * atoi(token_table[i]->operand[0]);
					printf("%04X	%s	%s	%s	%06X\n", locctr, token_table[i]->label, token_table[i]->operator,token_table[i]->total_operand, token_table[i]->object);

					locctr = locctr + 3;
				}
			}
			else if (!strcmp(token_table[i]->operator,"BYTE"))	//ù ���ڰ� X(16����, ���� 2���� 1����Ʈ), C(���� 1���� 1����Ʈ) �϶� ó��
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
						token_table[i]->object = token_table[i]->object << 8;					//����Ʈ �� ��ŭ object �ڵ� ����Ʈ��
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
						if (lit_table[lit_index].object != 0)				//�̹� ��ϵǾ� �ִٸ�. ����
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
				ltorg_line = i;					//ltorg ���� ����. ���� ltorg���� ���� ���ͷ��� �����ϵ��� �ؾ��ϱ⶧��
			}
			else
				printf("	%s	%s	%s	%s\n", token_table[i]->label, token_table[i]->operator,token_table[i]->total_operand, token_table[i]->comment);

		}
	}
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
			firstaddr = locctr;												//H���ڵ� ���� �����ּҸ� �����ϱ� ���� ����
			totalbyte = strtol(token_table[i]->operand, NULL, 16);
			fprintf(file, "H%-6s%06X%06X\n", token_table[i]->label, totalbyte, section_length[section_num]);		//H���ڵ� ���
		}

		if ((index = search_opcode(token_table[i]->operator)) > -1)		//opcode�� ������ �� ��ū�鿡�� ���� �󸶱��� �о������� ���
		{
			if (!strcmp(inst_table[index]->type, "1"))	// format 1 �� ��
			{
				count = 1;
				totalbyte = totalbyte + count;
				locctr = locctr + 1;
			}
			else if (!strcmp(inst_table[index]->type, "2")) // format 2 �� �� operand�� ���� �������Ϳ� ���� ����
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
		else						// opcode�� ���� directive �� ��
		{
			if (!strcmp(token_table[i]->operator,"WORD"))
			{
				count = 3;
				totalbyte = totalbyte + count;
				locctr = locctr + 3;
			}
			else if (!strcmp(token_table[i]->operator,"BYTE"))	//ù ���ڰ� X(16����, ���� 2���� 1����Ʈ), C(���� 1���� 1����Ʈ) �϶� ó��
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
						int addr = search_symbol(token_table[i]->operand[j]);				//�� �ּҸ� �� ���ڿ��� �޾ƿ�
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
			else if (!strcmp(token_table[i]->operator,"LTORG"))			//LTORG �� ���� ���� object �ڵ�� ���, �� �� ���ͷ��� ���� �ڵ� ���� ó��(���ͷ��� CSECT,END���� ���)
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
				checkindex = i;		//������ ������ ����

				/////////////////////���ͷ� �ڵ�

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
				/////////////////////���ͷ� �ڵ� ���

				for (int j = 0; j <= literal_line; j++)
					if (lit_table[j].section == section_num)
					{
						if(lit_table[j].litcheck == -1)
							fprintf(file, "%06X\n", lit_table[j].object);
						else
							fprintf(file, "%02X\n", lit_table[j].object);
					}


				for (int j = 0; j < modification_line; j++)						//modification record �� ������ ���
				{
					int x = mod_table[j].index;
					if (mod_table[j].section == section_num)
					{
						if (!strcmp(token_table[x]->operator,"WORD"))
						{
							int check = 0;
							for (int k = 0; k < strlen(token_table[x]->total_operand); k++)
							{
								if (token_table[x]->total_operand[k] == '-')						//- �� ������,
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
			else if (!strcmp(token_table[i]->operator,"CSECT"))	//Control section�� �ٲ� -> locctr �ʱ�ȭ, section_num���� �迭 ������ ����
			{
				if (hasliteral[section_num]==1)			//���ͷ��� ������ ���
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
					fprintf(file, "T%06X%02X", current_addr, totalbyte - count);				//������ �ٲ� �� ������� ���Ѱ͵� ���

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
								if (token_table[x]->total_operand[k] == '-')						//- �� ������,
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
			fprintf(file, "T%06X%02X", current_addr, totalbyte - count);					//�����ּ�, ������� ���Ͽ� �� ����Ʈ
			for (int j = checkindex; j < i; j++)											//30����Ʈ�� �ѱ������� ���
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
