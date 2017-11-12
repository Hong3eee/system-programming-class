import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

public class ResourceManager_R implements ResourceManager {

	public String progName[] = new String[3];
	public int startAddr[] = new int[3];
	public String startAddr_string[] = new String[3];
	public int progLength[] = new int[3];
	public String progLength_string[] = new String[3];
	public String firstInst;
	public boolean end = false;
	public String currentDevice = "";
	public int sectionIndex=0;
	public int beforeIndex=0;
	
	public Vector<String> memoryField = new Vector<>();		//현재 메모리의 위치를 gui상을 나타내기 위해 vector자료구조 사용
	public Vector<String> instField = new Vector<>();		//현재 명령어의 종류를 gui상을 나타내기 위해 vector자료구조 사용
	
	public File inputfile = null;
	public File outputfile = null;
	public FileInputStream inputStream = null;
	public FileOutputStream outputStream = null;

	public int A;
	public int X;
	public int L;
	public int PC;
	public int SW;		//비교를 하는데 참조하는 레지스터. > 일 때  2 ,== 1,< -1로 설정함 

	public int B;
	public int S;
	public int T;
	public int F;

	public int TA;

	public final static int MEM_SIZE = 0x100000 * 2; // XE 머신의 경우 메모리사이즈 -> 1Mbyte =
													// 2^20 byte 그런데 String으로 구현했으므로 String의 2바이트는 XE머신 메모리의 1바이트이다. 그러므로 2를 곱해줌.
	StringBuffer memory = new StringBuffer(MEM_SIZE);	// 그러므로 모든 주소 관련 연산에는 *2가 붙어있으며, 명시적으로 하기 위해 다항식을 풀어서 1*2 등으로 표현하였다.

	@Override	//메모리 *로 초기화
	public void initializeMemory() {
		for (int i = 0; i < MEM_SIZE; i++) {
			memory.insert(i, '*');
		}
	}

	@Override	//레지스터 값들 초기화
	public void initializeRegister() {
		A = 0;
		X = 0;
		L = 0;
		PC = 0;
		SW = 0;

		B = 0;
		S = 0;
		T = 0;
		F = 0;

		TA = 0;
	}

	@Override	//지정된 디바이스 'F1' 과 '05'로 입출력 스트림을 열어줌
	public void initialDevice() {
		try {
			inputStream = new FileInputStream(new File("F1.txt"));
			outputStream = new FileOutputStream(new File("05.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void writeDevice(String devName, int data) {
		if (devName.equals("05")) {
			try {
				outputStream.write(data);				//data 변수의 하위 1바이트만 write함
				outputStream.flush();					//스트림에 있는 버퍼를 파일로 보내줌. 
			} catch (IOException e) {		
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public int readDevice(String devName) {
		int read = -1;
		if (devName.equals("F1")) {
			try {
				read = inputStream.read();					//파일의 1바이트만 read해옴
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(read == -1)
				return 0;
			return read;
		}
		return read;
	}
	
	@Override		//큰 StringBuffer로 구현한 메모리의 값을 할당해주는 메소드. locate위치에 size 크기만큼 data를 입력해줌.
	public void setMemory(int locate, String data, int size) {
		memory.replace(locate, size, data);
	}

	// 각 레지스터 번호에 맞게 스위치로 분기하며
	// 그때의 각 레지스터 값을 리턴해줌. 
	@Override
	public void setRegister(int regNum, int value) {
		switch (regNum) {
		case 0:
			A = value;
			break;
		case 1:
			X = value;
			break;
		case 2:
			L = value;
			break;
		case 3:
			B = value;
			break;
		case 4:
			S = value;
			break;
		case 5:
			T = value;
			break;
		case 6:
			F = value;
			break;
		case 7:
			TA = value;
			break;
		case 8:
			PC = value;
			break;
		case 9:
			SW = value;
			break;
		}
	}

	@Override
	public String getMemory(int locate, int size) {
		return memory.substring(locate, size);
	}

	//레지스터의 값을 반환해준다
	@Override
	public int getRegister(int regNum) {
		switch(regNum){
		case 0:
			return A;
		case 1:
			return X;
		case 2:
			return L;
		case 3:
			return B;
		case 4:
			return S;
		case 5:
			return T;
		case 6:
			return F;
		case 7:
			return TA;
		case 8:
			return PC;
		case 9:
			return SW;
		}
		return -1;			// 에러발생
	}

}
