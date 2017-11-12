import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;

class inst_unit{
	String operator;
	String type;
	String opcode;
	int operandAmount;
}

public class SicSimulator_R implements SicSimulator {
	public SicLoader_R loader;
	public ResourceManager_R rMgr;
	int inst_index = 0;
	inst_unit inst_table[] = new inst_unit[256];
	@Override
	public void initialize(File objFile, ResourceManager_R rMgr) {
		this.rMgr = rMgr;
		rMgr.initializeMemory();			//rMgr의 메모리 및 레지스터, 디바이스 등을 초기화해준다
		rMgr.initializeRegister();
		rMgr.initialDevice();
		loader = new SicLoader_R();
		loader.load(objFile, rMgr);
		
		if(init_inst_file("inst.data")==-1)		//오브젝트 코드의 명령어를 비교하기 위한 명령어 테이블 만드는 메소드 호출
		{
			System.out.println("Inst file Init failed!!");	
			System.exit(0);	
		}
	
		
	}

	/* ----------------------------------------------------------------------------------
	* 설명 : 메모리에 올라간 오브젝트 코드를 파싱하고 분석하여 명령어를 수행시키는 메소드. 메소드 명 그대로 한 개의 명령어만 수행한다.
	* 매개 : -
	* 반환 : -
	* 주의 : 주소 관련 연산들이 *2 가 되어있는 부분이 있어서 혼동할 가능성이 있다.
	* 		메모리를 String으로 구현했으므로 String의 2바이트는 XE머신 메모리의 1바이트이다. 그러므로 2를 곱해준다.
	* ----------------------------------------------------------------------------------
	*/
	@Override
	public void oneStep() {			
		if(rMgr.end == true)		//오브젝트의 모든 명령어를 수행했음을 표시하는 boolean 변수 rMgr.end를 통해 프로그램이 끝났는지, 안끝났는지 판별. 끝났으면 추후의 로직을 실행하지 않는다.
			return ;
		int index = 0;
		int currentAddr = rMgr.getRegister(8);
		int opcode = Integer.parseInt(rMgr.memory.substring(currentAddr , currentAddr + 2),16);	//PC ~ PC+2 만큼 짤라서 확인
		int ni_bit = opcode & 3;	//opcode의 하위 2비트만 ni_bit로 가져옴
		int xbpe_bit = Integer.parseInt(rMgr.memory.substring(currentAddr + 2 , currentAddr + 3),16);
		opcode = opcode - ni_bit;	//opcode - ni_bit를 수행함으로써 optab과 비교할 값을 얻음.
		String opcode_string = String.format("%02X", opcode);

		
		
		if ((index = search_opcode(opcode_string)) > -1)		//opcode가 있을때 각 토큰들에선 현재 얼마까지 읽었는지만 기록
		{
			if (inst_table[index].type.equals("1"))	// format 1 일 때
			{
				rMgr.setRegister(8, rMgr.getRegister(8)+ 1 * 2);		//다음 PC값 증가
			}
			else if (inst_table[index].type.equals("2")) // format 2 일 때 operand로 오는 레지스터에 따라 구분
			{
				rMgr.setRegister(8, rMgr.getRegister(8)+ 2 * 2);		//다음 PC값 증가
				rMgr.memoryField.add(rMgr.memory.substring(currentAddr,	currentAddr+4));	//insturction GUI 부분에 나타낼 정보 저장
				switch(opcode) {
				case 0xB4 :  		//CLEAR
					rMgr.setRegister(Integer.parseInt(rMgr.memory.substring(currentAddr+2,	currentAddr+3),16), 0);
					rMgr.setRegister(7, 0);		//2형식은 타겟이 따로 없으므로 0으로 설정해둠
					rMgr.instField.add("CLEAR");	//LOG GUI 부분에 나타날 명령어 이름 저장
					break;
				case 0xA0: 			// COMPR
					int comp1 = rMgr.getRegister(Integer.parseInt(rMgr.memory.substring(currentAddr + 2, currentAddr + 3), 16));
					int comp2 = rMgr.getRegister(Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 4), 16));	//레지스터 두개 안에 있는 값들 비교
					rMgr.setRegister(7, 0);		//2형식은 타겟이 따로 없으므로 0으로 설정해둠
					if (comp1 > comp2)
						rMgr.setRegister(9, 2);
					if (comp1 == comp2)
						rMgr.setRegister(9, 1);
					if (comp1 < comp2)
						rMgr.setRegister(9, -1);
					rMgr.instField.add("COMPR");	//LOG GUI 부분에 나타날 명령어 이름 저장
					break;
				case 0xB8 :  		//TIXR
					rMgr.setRegister(1, rMgr.getRegister(1) + 1 * 2);			// X도 주소 관련해서 쓰기 때문에 2를 곱해서 더해줌
					rMgr.setRegister(7, 0);		//2형식은 타겟이 따로 없으므로 0으로 설정해둠
					if(rMgr.getRegister(1) > rMgr.getRegister(Integer.parseInt(rMgr.memory.substring(currentAddr + 2, currentAddr + 3), 16)))
						rMgr.setRegister(9, 2);
					if(rMgr.getRegister(1) == rMgr.getRegister(Integer.parseInt(rMgr.memory.substring(currentAddr + 2, currentAddr + 3), 16)))
						rMgr.setRegister(9, 1);
					if(rMgr.getRegister(1) < rMgr.getRegister(Integer.parseInt(rMgr.memory.substring(currentAddr + 2, currentAddr + 3), 16)))
						rMgr.setRegister(9, -1);
					rMgr.instField.add("TIXR");	//LOG GUI 부분에 나타날 명령어 이름 저장
					break;
				}
			}
			else if (inst_table[index].type.equals("3/4"))
			{
				int targetAddr=0;
				int x_flag=0,b_flag=0,p_flag=0,e_flag=0;
				int temp_sum = 0;
				int sign=0;
				// target 주소를 구하기 위해  x, b, p, e flag를 설정.
				x_flag = (xbpe_bit & 8) >> 3;
				b_flag = (xbpe_bit & 4) >> 2;
				p_flag = (xbpe_bit & 2) >> 1;
				e_flag = xbpe_bit & 1;
				
				if( e_flag == 0){
					rMgr.setRegister(8, rMgr.getRegister(8)+ 3 * 2);	//다음 PC값 증가	모든 PC값들도 *2를 해서 증가시켜줌
					rMgr.memoryField.add(rMgr.memory.substring(currentAddr,	currentAddr+6));	//insturction GUI 부분에 나타낼 정보 저장
				}
				else if(e_flag == 1){
					rMgr.setRegister(8, rMgr.getRegister(8)+ 4 * 2);	//다음 PC값 증가
					rMgr.memoryField.add(rMgr.memory.substring(currentAddr,	currentAddr+8));	//insturction GUI 부분에 나타낼 정보 저장
				}
					
				//Target address 설정
				if (x_flag == 1)
					temp_sum += rMgr.getRegister(1); 		//X를 증가시킬 때, 2를 곱해서 증가시켜놨음에 유의
				if (e_flag == 1) {
					;
				} else {
					if (b_flag == 1)
						temp_sum += rMgr.getRegister(3);
					if (p_flag == 1)
						temp_sum += rMgr.getRegister(8);
				}
				switch(opcode) {
				case 0x00 :  //LDA
					if (e_flag == 0)	// 3형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 6), 16) * 2;	//모든 타겟 주소 역시 *2상태로 처리.
					else 				//4형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 8), 16) * 2;
					targetAddr = targetAddr + temp_sum;
					rMgr.setRegister(7, targetAddr);
					if (ni_bit == 3) {
						rMgr.setRegister(0, Integer.parseInt(rMgr.memory.substring(targetAddr, targetAddr + 6), 16));
					} else if (ni_bit == 2) {
						;
					} else if (ni_bit == 1) { // immediate로 바로 A레지스터에 저장
						rMgr.setRegister(0, targetAddr/2);
					}
					rMgr.instField.add("LDA");	//LOG GUI 부분에 나타날 명령어 이름 저장
					break;
				case 0x0C :  //STA
					if (e_flag == 0)	// 3형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 6), 16) * 2;
					else 				//4형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 8), 16) * 2;
					targetAddr = targetAddr + temp_sum;
					rMgr.setRegister(7, targetAddr);
					
					if (ni_bit == 3) {
						rMgr.memory.replace(targetAddr, targetAddr+6, String.format("%06X",rMgr.getRegister(0)));//메모리의 형태가 STring으로 구현했으므로 메모리에 String.format을 이용해 스트링 형태로 저장한다.
					} else if (ni_bit == 2) {
						;
					} else if (ni_bit == 1) { 
						;
					}
					rMgr.instField.add("STA");	//LOG GUI 부분에 나타날 명령어 이름 저장
					break;
				case 0x10 :  //STX
					if (e_flag == 0)	// 3형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 6), 16) * 2;
					else 				//4형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 8), 16) * 2;
					targetAddr = targetAddr + temp_sum;
					rMgr.setRegister(7, targetAddr);
					
					if (ni_bit == 3) {
						rMgr.memory.replace(targetAddr, targetAddr+6, String.format("%06X",rMgr.getRegister(1)/2));//X를 2를 곱해서 증가시켰지만 실제 메모리에 저장할 땐, 2를 나눠서 저장시킴.
					} else if (ni_bit == 2) {																		// 추후 다른 레지스터에서 사용 할때 2곱해서 불러오도록 코딩함.
						;
					} else if (ni_bit == 1) { 
						;
					}
					rMgr.instField.add("STX");	//LOG GUI 부분에 나타날 명령어 이름 저장
					break;

				case 0x14 :  //STL
					if (e_flag == 0)	// 3형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 6), 16) * 2;
					else 				//4형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 8), 16) * 2;
					targetAddr = targetAddr + temp_sum;
					rMgr.setRegister(7, targetAddr);
					
					if (ni_bit == 3) {
						rMgr.memory.replace(targetAddr, targetAddr+6, String.format("%06X",rMgr.getRegister(2)));//메모리의 형태가 STring으로 구현했으므로 메모리에 String.format을 이용해 스트링 형태로 저장한다.
					} else if (ni_bit == 2) {
						;
					} else if (ni_bit == 1) { 
						;
					}
					rMgr.instField.add("STL");	//LOG GUI 부분에 나타날 명령어 이름 저장
					break;

				case 0x28 :  //COMP
					if (e_flag == 0)	// 3형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 6), 16) * 2;
					else 				//4형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 8), 16) * 2;
					targetAddr = targetAddr + temp_sum;
					rMgr.setRegister(7, targetAddr);
					
					if (ni_bit == 3) {		//값들을 비교하여 SW 레지스터에 논리결과를 저장시킴. 이를 이용해 다른 명령어 수행
						int comp = Integer.parseInt(rMgr.memory.substring(targetAddr, targetAddr + 6), 16);
						if (comp > rMgr.getRegister(0))
							rMgr.setRegister(9, 2);
						if (comp == rMgr.getRegister(0))
							rMgr.setRegister(9, 1);
						if (comp < rMgr.getRegister(0))
							rMgr.setRegister(9, -1);
					} else if (ni_bit == 2) {
						;
					} else if (ni_bit == 1) { 
						if(targetAddr > rMgr.getRegister(0))
							rMgr.setRegister(9, 2);
						if(targetAddr == rMgr.getRegister(0))
							rMgr.setRegister(9, 1);
						if(targetAddr < rMgr.getRegister(0))
							rMgr.setRegister(9, -1);
					}
					rMgr.instField.add("COMP");	//LOG GUI 부분에 나타날 명령어 이름 저장
					break;
				case 0x30 :  //JEQ
					if (e_flag == 0)	// 3형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 6), 16) * 2;
					else 				//4형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 8), 16) * 2;
					
					sign = targetAddr & 0x800;		//주소 3바이트의  맨 앞자리가 1. 즉 음수라면.
					if(sign == 0x800)				// 3바이트 앞의 모든 바이트를 1로 맵핑시켜서 음수로 만든다
					{
						targetAddr = targetAddr | 0xFFFFF000;
					}
					targetAddr = targetAddr + temp_sum;
					rMgr.setRegister(7, targetAddr);
					
					if(rMgr.getRegister(9) == 1)			// 같다면 다음 PC값에 targetAddr을 할당해 줌. = Jump의 기능
						rMgr.setRegister(8, targetAddr);
					rMgr.instField.add("JEQ");	//LOG GUI 부분에 나타날 명령어 이름 저장
					break;
				case 0x38 :  //JLT
					if (e_flag == 0)	// 3형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 6), 16) * 2;
					else 				//4형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 8), 16) * 2;
					sign = targetAddr & 0x800;		//주소 3바이트의  맨 앞자리가 1. 즉 음수라면.
					if(sign == 0x800)				// 3바이트 앞의 모든 바이트를 1로 맵핑시켜서 음수로 만든다
					{
						targetAddr = targetAddr | 0xFFFFF000;
					}
					targetAddr = targetAddr + temp_sum;
					rMgr.setRegister(7, targetAddr);
					
					if(rMgr.getRegister(9) == -1)			// 작다면 다음 PC값에 targetAddr을 할당해 줌. = Jump의 기능
						rMgr.setRegister(8, targetAddr);
					rMgr.instField.add("JLT");	//LOG GUI 부분에 나타날 명령어 이름 저장
					break;
				case 0x3C :  //J
					if (e_flag == 0)	// 3형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 6), 16) * 2;
					else 				//4형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 8), 16) * 2;
					
					sign = targetAddr & 0x800;		//주소 3바이트의  맨 앞자리가 1. 즉 음수라면.
					if(sign == 0x800)				// 3바이트 앞의 모든 바이트를 1로 맵핑시켜서 음수로 만든다
					{
						targetAddr = targetAddr | 0xFFFFF000;
					}
					targetAddr = targetAddr + temp_sum;
					rMgr.setRegister(7, targetAddr);

					if (ni_bit == 3) {						// PC값에 targetAddr을 할당해 줌. = Jump의 기능
						rMgr.setRegister(8, targetAddr);
					} else if (ni_bit == 2) {
						rMgr.setRegister(8, Integer.parseInt(rMgr.memory.substring(targetAddr, targetAddr + 6), 16) * 2);
					} else if (ni_bit == 1) { 
						rMgr.setRegister(8, targetAddr);
					}
					
					if(rMgr.getRegister(8) == rMgr.startAddr[0])//J 혹은 RSUB로 메인 프로그램이 끝나면 맨 처음 시작주소로 PC값이 할당된다. 이를 이용해 프로그램의 끝을 알린다.
						rMgr.end = true;
					rMgr.instField.add("J");	//LOG GUI 부분에 나타날 명령어 이름 저장
					break;
				case 0x48 :  //JSUB
					targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 8), 16) * 2;
					targetAddr = targetAddr + temp_sum;
					rMgr.setRegister(7, targetAddr);
					
					rMgr.setRegister(2, rMgr.getRegister(8));		//JSUB의 명세대로 현재 PC값을 L 레지스터에 저장 후, target주소를 PC값에 할당
					rMgr.setRegister(8, targetAddr);
					
					for(int x= 0; x<3;x++){					//JSUB를 수행하면 controlSection이 바뀌므로 GUI상에 나타나는 프로그램 이름, 시작주소 등의 정보가 바뀌어야한다.
						if(targetAddr == rMgr.startAddr[x]) //그러므로 index값을 설정해줘서 바뀌도록 프로그래밍 하였다.
						{
							rMgr.beforeIndex = rMgr.sectionIndex;
							rMgr.sectionIndex = x;
						}
					}
					
					rMgr.instField.add("JSUB");	//LOG GUI 부분에 나타날 명령어 이름 저장
					break;
				case 0x4C :  //RSUB
					if (e_flag == 0)	// 3형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 6), 16) * 2;
					else 				//4형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 8), 16) * 2;
					targetAddr = targetAddr + temp_sum;
					rMgr.setRegister(7, targetAddr);
					
					rMgr.setRegister(8, rMgr.getRegister(2));		//pc에 (L)값을 넣어줌
					if(rMgr.getRegister(8) == rMgr.startAddr[0])	//J 혹은 RSUB로 메인 프로그램이 끝나면 맨 처음 시작주소로 PC값이 할당된다. 이를 이용해 프로그램의 끝을 알린다.
						rMgr.end = true;
					rMgr.currentDevice = "";
					rMgr.sectionIndex = rMgr.beforeIndex;		//JSUB로 서브루틴 부르기 전의 index로 돌아간다.
					rMgr.instField.add("RSUB");	//LOG GUI 부분에 나타날 명령어 이름 저장
					break;
				case 0x50 :  //LDCH
					if (e_flag == 0)	// 3형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 6), 16) * 2;
					else 				//4형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 8), 16) * 2;
					targetAddr = targetAddr + temp_sum;
					rMgr.setRegister(7, targetAddr);
					if (ni_bit == 3) {
						rMgr.setRegister(0, Integer.parseInt(rMgr.memory.substring(targetAddr, targetAddr + 2), 16));
					} else if (ni_bit == 2) {
						;
					} else if (ni_bit == 1) { // immediate로 바로 A레지스터에 저장
						rMgr.setRegister(0, targetAddr);
					}
					rMgr.instField.add("LDCH");	//LOG GUI 부분에 나타날 명령어 이름 저장
					break;
				case 0x54 :  //STCH
					if (e_flag == 0)	// 3형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 6), 16) * 2;
					else 				//4형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 8), 16) * 2;
					targetAddr = targetAddr + temp_sum;
					rMgr.setRegister(7, targetAddr);
					
					if (ni_bit == 3) {
						rMgr.memory.replace(targetAddr, targetAddr+2, String.format("%02X",rMgr.getRegister(0)));//메모리의 형태가 STring으로 구현했으므로 메모리에 String.format을 이용해 스트링 형태로 저장한다.
					} else if (ni_bit == 2) {
						;
					} else if (ni_bit == 1) { 
						;
					}
					rMgr.instField.add("STCH");	//LOG GUI 부분에 나타날 명령어 이름 저장
					break;
				case 0x74 :  //LDT
					if (e_flag == 0)	// 3형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 6), 16) * 2;
					else 				//4형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 8), 16) * 2;
					targetAddr = targetAddr + temp_sum;
					rMgr.setRegister(7, targetAddr);
					if (ni_bit == 3) {
						rMgr.setRegister(5, Integer.parseInt(rMgr.memory.substring(targetAddr, targetAddr + 6), 16) * 2);
					} else if (ni_bit == 2) {
						;
					} else if (ni_bit == 1) { // immediate로 바로 A레지스터에 저장
						rMgr.setRegister(5, targetAddr);
					}
					rMgr.instField.add("LDT");	//LOG GUI 부분에 나타날 명령어 이름 저장
					break;
					
				case 0xD8 :  //RD
					if (e_flag == 0)	// 3형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 6), 16) * 2;
					else 				//4형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 8), 16) * 2;
					targetAddr = targetAddr + temp_sum;
					rMgr.setRegister(7, targetAddr);
					rMgr.setRegister(0, rMgr.readDevice("F1"));
					rMgr.instField.add("RD");	//LOG GUI 부분에 나타날 명령어 이름 저장
					break;
				case 0xDC :  //WD
					if (e_flag == 0)	// 3형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 6), 16) * 2;
					else 				//4형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 8), 16) * 2;
					targetAddr = targetAddr + temp_sum;
					rMgr.setRegister(7, targetAddr);
					rMgr.writeDevice("05",  rMgr.getRegister(0));
					rMgr.instField.add("WD");	//LOG GUI 부분에 나타날 명령어 이름 저장
					break;
				case 0xE0 :  //TD
					if (e_flag == 0)	// 3형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 6), 16) * 2;
					else 				//4형식
						targetAddr = Integer.parseInt(rMgr.memory.substring(currentAddr + 3, currentAddr + 8), 16) * 2;
					targetAddr = targetAddr + temp_sum;
					rMgr.setRegister(7, targetAddr);
					if (Integer.parseInt(rMgr.memory.substring(targetAddr, targetAddr + 2), 16) == 0xF1) {//입출력 지정 장치인 F1 혹은 05 라면 flag값 -1로 설정
						rMgr.setRegister(9, -1);
						rMgr.currentDevice = "F1";			//TD명령어 부터 CurrentDevice 를 GUI에 뿌려줌
					}
					if (Integer.parseInt(rMgr.memory.substring(targetAddr, targetAddr + 2), 16) == 0x05) {
						rMgr.setRegister(9, -1);
						rMgr.currentDevice = "05";
					}
					rMgr.instField.add("TD");	//LOG GUI 부분에 나타날 명령어 이름 저장	
					break;
				}
			}
		}
	}
	
	
	/* ----------------------------------------------------------------------------------
	* 설명 : 현재 PC 값부터 프로그램이 끝날 때까지 명령어를 수행한다.
	* 매개 : -
	* 반환 : -
	* ----------------------------------------------------------------------------------
	*/
	@Override
	public void allStep() {
		
		while(true){			//다시 PC값이 시작주소로 할당되면  프로그램이 종료
			if(rMgr.end){
				break;
			}
			oneStep();
		}
	}
	
	/* ----------------------------------------------------------------------------------
	* 설명 : 머신을 위한 기계 코드목록 파일을 읽어 기계어 목록 테이블(inst_table)을
	*        생성하는 함수이다.
	* 매개 : 기계어 목록 파일
	* 반환 : 정상종료 = 0 , 에러 < 0
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
	* 설명 : 입력 문자열이 기계어 코드인지를 검사하는 함수이다.
	* 매개 : 토큰 단위로 구분된 문자열
	* 반환 : 정상종료 = 기계어 테이블 인덱스, 에러 < 0
	* ----------------------------------------------------------------------------------
	*/
	int search_opcode(String str)	//str을 비교값으로 해서 inst_table을 모두 뒤져서 index를 찾음. 없으면 -1 리턴
	{
		for (int i = 0; i <= inst_index; i++)
		{
			if (str.equals(inst_table[i].opcode))
				return i;
		}

		return -1;
	}
}
