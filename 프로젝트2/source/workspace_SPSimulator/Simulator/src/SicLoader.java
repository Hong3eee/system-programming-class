import java.io.File;

public interface SicLoader {
    //목적코드를 읽어 메모리에 로드한다.
    //목적코드의 각 헤더(H, T, M 등)를 읽어 동작을 수행한다.
    public void load(File objFile, ResourceManager_R rMgr);
}