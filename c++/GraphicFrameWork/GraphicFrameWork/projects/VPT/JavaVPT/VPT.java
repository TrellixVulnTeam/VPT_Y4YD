class VPT  {  
static{
    System.load("C:\\Users\\richa\\source\\repos\\GraphicFrameWork\\GraphicFrameWork\\projects\\VPT\\JavaVPT\\Client.dll");
}
static native void main1(String args[]);
static native void forceLogout();
static native void socketClosed();
public static void CallBack(){System.out.println("TEST");};
public static void main(String args[]) {  
     main1(args);
}  
}  