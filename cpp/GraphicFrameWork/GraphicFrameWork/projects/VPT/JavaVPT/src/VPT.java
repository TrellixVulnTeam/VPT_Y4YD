
import java.io.File;

class VPT  {  
static{
    //See client.ClientJNI for explination
    String libDir = new File(".").getAbsolutePath() + File.separator;
    String[] libraries = {"zlib1", "libfreetype-6", "libjpeg-9", "libpng16-16",
        "libtiff-5", "libwebp-7", "SDL2", "SDL2_image", "SDL2_ttf", "Client"};
    String extension = ".dll";
    for (String library : libraries) {
        System.load(libDir + library + extension);
    }
}
static native void cppMain(String args[]);
static native void recievePacket(Object obj);
static native void socketClosed();
public static void CallBack(){System.out.println("TEST");};
public static void main(String args[]) {  
     cppMain(args);
}  
}  