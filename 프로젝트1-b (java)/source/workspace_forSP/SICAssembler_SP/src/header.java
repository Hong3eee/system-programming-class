class inst_unit{
	String operator;
	String type;
	String opcode;
	int operandAmount;
}

class token_unit{
	String label="";
	String operator="";
	String operand[] = new String[3];
	String comment="";
	char nixbpe=0; // 추후 프로젝트에서 사용된다. 1 2 4 8 16 32
	
	//추가
	int current_locctr;
	int pcValue;			
	String total_operand="";
	int object=0;
	int hasobjectcode=0;
}

class symbol_unit {
	String symbol;
	int addr;
	int section;
	char sign;
}

class literal_unit {
	String literal;
	int addr;
	int section;
	int object;
}

class modification_unit {
	int index;
	int section;
}