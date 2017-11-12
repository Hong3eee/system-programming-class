/*
* my_assembler �Լ��� ���� ���� ���� �� ��ũ�θ� ��� �ִ� ��� �����̴�.
*
*/
#define MAX_INST 256
#define MAX_LINES 5000
#define MAX_OPERAND 3

//nixbpe �� �ڸ��� ���� ����.
#define N 32
#define I 16
#define X 8
#define B 4
#define P 2
#define E 1


/*
* instruction ��� ���Ϸ� ���� ������ �޾ƿͼ� �����ϴ� ����ü �����̴�.
* ������ ������ instruction set�� ��Ŀ� ���� ���� �����ϵ�
* ���� ���� �ϳ��� instruction�� �����Ѵ�.
*/
struct inst_unit {
	/* add your code here */
	char * operator;
	char * type;
	char * opcode;
	int operandAmount;
};
typedef struct inst_unit inst;
inst *inst_table[MAX_INST];
int inst_index;

/*
* ����� �� �ҽ��ڵ带 �Է¹޴� ���̺��̴�. ���� ������ ������ �� �ִ�.
*/
char *input_data[MAX_LINES];
static int line_num;

int label_num;

/*
* ����� �� �ҽ��ڵ带 ��ū������ �����ϱ� ���� ����ü �����̴�.
* operator�� renaming�� ����Ѵ�.
* nixbpe�� 8bit �� ���� 6���� bit�� �̿��Ͽ� n,i,x,b,p,e�� ǥ���Ѵ�.
*/
struct token_unit {
	char *label;
	char *operator;
	char *operand[MAX_OPERAND];
	char *comment;
	char nixbpe; // ���� ������Ʈ���� ���ȴ�. 1 2 4 8 16 32
	
	//�߰�
	int current_locctr;
	int pcValue;			
	char total_operand[100];
	int object;
	int hasobjectcode;
};

typedef struct token_unit token;
token *token_table[MAX_LINES];
static int token_line;

/*
* �ɺ��� �����ϴ� ����ü�̴�.
* �ɺ� ���̺��� �ɺ� �̸�, �ɺ��� ��ġ�� �����ȴ�.
*/
struct symbol_unit {
	char symbol[10];
	int addr;
	int section;
	char sign;
};

typedef struct symbol_unit symbol;
symbol sym_table[MAX_LINES];

struct literal_unit {
	char literal[10];
	int addr;
	int section;
	int litcheck;

	int object;
};

typedef struct literal_unit literal;
literal lit_table[100];				//���ͷ� ���̺��� ũ������ ���� ����ǹǷ� 100���� ���� ����

struct modification_unit {
	int index;
	int section;
};

typedef struct modification_unit modification;
modification mod_table[100];

int hasliteral[10] = { 0, };
int modification_index[100] = { -1, };
int modification_line = 0;
int symbol_line = -1;
int literal_line = -1;
int section_num = 0;
static int locctr;

int section_length[10] = { 0, };

static char *input_file;
static char *output_file;
int init_my_assembler(void);
int init_inst_file(char *inst_file);
int init_input_file(char *input_file);
int search_opcode(char *str);
void make_opcode_output(char *file_name);

/* �߰��� �����Ͽ� ������Ʈ���� ����ϰ� �Ǵ� �Լ�*/
int manage_symbol(char *str);
int manage_literal(char *str);
int search_symbol(char *str);
int search_literal(char *str);
int search_literal_index(char *str);
int search_symbol_index(char *str);

static int assem_pass1(void);
static int assem_pass2(void);
void make_objectcode_output(char *file_name);