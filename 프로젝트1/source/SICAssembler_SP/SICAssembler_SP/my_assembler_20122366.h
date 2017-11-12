/*
* my_assembler 함수를 위한 변수 선언 및 매크로를 담고 있는 헤더 파일이다.
*
*/
#define MAX_INST 256
#define MAX_LINES 5000
#define MAX_OPERAND 3

//nixbpe 각 자리에 따른 값들.
#define N 32
#define I 16
#define X 8
#define B 4
#define P 2
#define E 1


/*
* instruction 목록 파일로 부터 정보를 받아와서 생성하는 구조체 변수이다.
* 구조는 각자의 instruction set의 양식에 맞춰 직접 구현하되
* 라인 별로 하나의 instruction을 저장한다.
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
* 어셈블리 할 소스코드를 입력받는 테이블이다. 라인 단위로 관리할 수 있다.
*/
char *input_data[MAX_LINES];
static int line_num;

int label_num;

/*
* 어셈블리 할 소스코드를 토큰단위로 관리하기 위한 구조체 변수이다.
* operator는 renaming을 허용한다.
* nixbpe는 8bit 중 하위 6개의 bit를 이용하여 n,i,x,b,p,e를 표시한다.
*/
struct token_unit {
	char *label;
	char *operator;
	char *operand[MAX_OPERAND];
	char *comment;
	char nixbpe; // 추후 프로젝트에서 사용된다. 1 2 4 8 16 32
	
	//추가
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
* 심볼을 관리하는 구조체이다.
* 심볼 테이블은 심볼 이름, 심볼의 위치로 구성된다.
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
literal lit_table[100];				//리터럴 테이블은 크지않을 것이 예상되므로 100으로 임의 지정

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

/* 추가로 구현하여 프로젝트에서 사용하게 되는 함수*/
int manage_symbol(char *str);
int manage_literal(char *str);
int search_symbol(char *str);
int search_literal(char *str);
int search_literal_index(char *str);
int search_symbol_index(char *str);

static int assem_pass1(void);
static int assem_pass2(void);
void make_objectcode_output(char *file_name);