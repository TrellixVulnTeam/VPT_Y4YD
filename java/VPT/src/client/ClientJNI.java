package client;

import common.networking.packet.Packet;

/**
 * A class containing JNI method calls for the client
 */
public final class ClientJNI {
    
    static {
        System.loadLibrary("Client.dll");
    }
    
    /**
     * Starts the native client code. This is called once a server connection has been established
     * @param args the command line arguments passed to the program
     */
    public static native void cppMain(String[] args);
    /**
     * Notifies the native client code of a received packet from the server
     * @param packet the packet received from the server
     */
    public static native void recievePacket(Packet packet);
    /**
     * Notifies the native client code that the server has terminated its connection
     */
    public static native void socketClosed();
    
    private ClientJNI() {}
    
}