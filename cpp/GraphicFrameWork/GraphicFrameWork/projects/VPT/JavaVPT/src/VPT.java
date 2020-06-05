
import java.io.File;

class VPT  {  
static{
    String dir = new File(".\\src").getAbsolutePath();
    System.load(dir + "\\zlib1.dll");
    for(File library: new File(dir).listFiles()) {
        String path = library.getAbsolutePath();
        if(path.endsWith(".dll") && !path.endsWith("Client.dll") && !path.endsWith("zlib1.dll")) {
            System.out.println(path);
            System.load(path);
        }
    }
    System.load(dir + "\\Client.dll");
}
static native void main1(String args[]);
static native void forceLogout();
static native void socketClosed();
public static void CallBack(){System.out.println("TEST");};
public static void main(String args[]) {  
     main1(args);
}  
}  