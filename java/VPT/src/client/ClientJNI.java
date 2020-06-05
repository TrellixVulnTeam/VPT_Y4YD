package client;

import common.networking.packet.Packet;

public final class ClientJNI {
    
    static {
        System.loadLibrary("Client.dll");
    }
    
    public static native void main(String[] args);
    public static native void recievePacket(Packet packet);
    public static native void socketClosed();
    
    private ClientJNI() {}
    
}