package common.networking;

final class NetworkingJNI {
    
    static {
        System.loadLibrary("Common.dll");
    }
    
    static native String createClient();
    static native void dispose(String handle);
    static native void sendData(String handle, byte[] data, int length);
    static native int recieveData(String handle);
    static native String createServer();
    static native String accept(String handle);
    
    private NetworkingJNI() {}
}