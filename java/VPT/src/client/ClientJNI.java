package client;

public final class ClientJNI {
    
    static {
        System.loadLibrary("Client.dll");
    }
    
    public static native void main(String[] args);
    public static native void forceLogout();
    public static native void socketClosed();
    
    private ClientJNI() {}
    
}