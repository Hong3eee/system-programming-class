import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

class Modify {
	String name[] = new String[15];
	int addr[] = new int[15];
	int count[] = new int[15];
	char sign[] = new char[15];
	int section[] = new int[15];
}

class Define {
	String name[] = new String[6];
	int addr[] = new int[6];
}

public class SicLoader_R implements SicLoader {
	ResourceManager_R rMgr;
	// public BufferedReader reader;

	Define define = new Define();		//D 레코드와 M 레코드에 쓸 자료구조
	Modify modify = new Modify();

	/* ----------------------------------------------------------------------------------
	* 설명 : 가상으로 잡은 rMgr.memory 에 인풋으로 들어온 Object Program을 로드시키는 함수.
	* 	       각 섹션을 참고하여 순서대로 로드해준다. 그 중에 M레코드가 들어오면 실제 메모리에는 수정된 주소를 업데이트 해줘야 하므로
	* 	       이 또한 처리하였다.
	* 매개 : -
	* 반환 : -
	* ----------------------------------------------------------------------------------
	*/
	@Override
	public void load(File objFile, ResourceManager_R rMgr) {

		this.rMgr = rMgr;
		String line;
		int section_num = 0, d_num = 0, m_num = 0;

		try {
			Scanner scanner = new Scanner(objFile);
			while (scanner.hasNextLine()) {			//인풋인 오브젝트 파일을 끝까지 읽어들이면서 메모리에 로드함
				line = scanner.nextLine();
				if (line.equals(""))
					continue;

				if (line.charAt(0) == 'H') {		//H태그의 각종 정보들을 처리.
					rMgr.progName[section_num] = line.substring(1, 7);
					rMgr.progLength[section_num] = Integer.parseInt(line.substring(13, 19), 16) * 2;	//StringBuffer의 1바이트엔  half_byte짜리 캐릭터가 할당되므로 *2를 해줌.
					rMgr.progLength_string[section_num] = String.format("%06X", rMgr.progLength[section_num]);

					if (section_num > 0) {		// 첫번째 섹션이 아니라면 startAddr[0] + 각 프로그램의 길이를 더해준다.
						int total = 0;

						for (int x = 0; x < section_num; x++)
							total = total + rMgr.progLength[x];

						rMgr.startAddr[section_num] = total;	
						//StartAddr을 이용하여 뒤에 따라오는 컨트롤 섹션들의 실제 메모리 값 할당.
						rMgr.startAddr_string[section_num] = String.format("%06X", rMgr.startAddr[section_num]/2);

						define.name[d_num] = line.substring(1, 7);
						define.addr[d_num++] = rMgr.startAddr[section_num]/2;		
						//define의 주소는 StringBuffer의 번지수를 찾아가는 게 아니라 그 안에 들어갈 내용물. *2를 해줄 필요가 없다

					} else {		 //첫번째 섹션이라면 startAddr의 값을 H레코드의 시작 주소로 초기화
						rMgr.startAddr[section_num] = Integer.parseInt(line.substring(7, 13), 16) * 2;
						rMgr.startAddr_string[section_num] = String.format("%06X", rMgr.startAddr[section_num]/2);
					}
				} else if (line.charAt(0) == 'E') {		//E 태그의 뒤에따라오는 인자는 첫번째 실행가능한 명령어. 이를 처리.
					if (line.length() != 1)
						rMgr.firstInst = line.substring(1, 7);
					section_num++;
				} else if (line.charAt(0) == 'D') {		//D태그를 만나면 define 클래스의 변수들에 저장해줌. 뒤에서 Modificaion record가 가르키는 메모리 위치에 맵핑함
					define.name[d_num] = line.substring(1, 7);
					define.addr[d_num++] = Integer.parseInt(line.substring(7, 13), 16);
					define.name[d_num] = line.substring(13, 19);
					define.addr[d_num++] = Integer.parseInt(line.substring(19, 25), 16);
					define.name[d_num] = line.substring(25, 31);
					define.addr[d_num++] = Integer.parseInt(line.substring(31), 16);
				} else if (line.charAt(0) == 'M') {		//M태그를 따로 modify 클래스 변수에 저장. 메모리에 일단 T 태그의 레코드를 다 올린 후, 추후 메모리 접근하여 수정함.
					modify.addr[m_num] = Integer.parseInt(line.substring(1, 7), 16) * 2;
					modify.count[m_num] = Integer.parseInt(line.substring(7, 9), 16);
					if (line.contains("-"))
						modify.sign[m_num] = '-';
					else
						modify.sign[m_num] = '+';
					modify.name[m_num] = line.substring(10);
					modify.section[m_num] = section_num;
					m_num++;
				} else if (line.charAt(0) == 'T') {		//T 레코드의 값들을 넣어줌. StringBuffer의 1바이트엔  half_byte짜리 캐릭터가 할당되므로 *2를 해줌.
					int locate = Integer.parseInt(line.substring(1, 7), 16) * 2 + rMgr.startAddr[section_num];
					int size = Integer.parseInt(line.substring(7, 9), 16) * 2;
					rMgr.setMemory(locate, line.substring(9, 9 + size), locate + size);
				}
			}

			for (int i = 0; i < m_num; i++)		//modify와 define을 각각 모두 비교하면서 주소값을 갱신해줌 
				for (int j = 0; j < d_num; j++) {
					if (modify.name[i].equals(define.name[j])) {
						int start = rMgr.startAddr[modify.section[i]];	//각  M 레코드 마다 어느 section에 속해있는지를 기준으로 주소계산.
						if (modify.count[i] == 5) {	//count에 따라서 주소할당의 방식이 다르다. 5라면 half-byte로 인해서 +1을 해줘서 계산한다.
							rMgr.setMemory(start + modify.addr[i] + 1, String.format("%05X", define.addr[j]),
									start + modify.addr[i] + 1 + modify.count[i]);
						} else if (modify.count[i] == 6) {
							if (modify.sign[i] == '-') {	//음수라면, 먼저 있던 값 - 찾은 주소 값을 해서 넣어준다.
								int original_value = Integer.parseInt(rMgr.memory.substring(start + modify.addr[i],
										start + modify.addr[i] + modify.count[i]), 16);
								rMgr.setMemory(start + modify.addr[i],
										String.format("%06X", original_value - define.addr[j]),
										start + modify.addr[i] + modify.count[i]);
							} else if (modify.sign[i] == '+') {	//양수라면 바로 넣어준다.
								rMgr.setMemory(start + modify.addr[i], String.format("%06X", define.addr[j]),
										start + modify.addr[i] + modify.count[i]);
							}
						}
					}
				}
			scanner.close();		//스트림 닫아줌
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
