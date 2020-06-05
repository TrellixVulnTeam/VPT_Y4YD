package client;

import common.networking.packet.Packet;
import java.io.File;

/**
 * A class containing JNI method calls for the client
 */
public final class ClientJNI {
    
    static {
        //The directory holding the libraries
        String libDir = new File(".").getAbsolutePath() + File.separator;
        //List of required libraries in the ORDER that they should be loaded
        String[] libraries = {"zlib1", "libfreetype-6", "libjpeg-9", "libpng16-16",
            "libtiff-5", "libwebp-7", "SDL2", "SDL2_image", "SDL2_ttf", "Client"};
        //Library file extension
        String extension = ".dll";
        for(String library: libraries) {
            System.load(libDir + library + extension);
        }
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