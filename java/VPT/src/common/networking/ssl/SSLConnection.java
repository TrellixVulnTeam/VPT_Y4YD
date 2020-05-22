package common.networking.ssl;

import common.networking.packet.PacketInputStream;
import common.networking.packet.PacketOutputStream;
import java.io.IOException;
import javax.net.ssl.SSLSocket;

/**
 * A class binding a {@link SSLSocket} to a {@link PacketInputStream} and {@link PacketOutputStream}
 */
public class SSLConnection {
    
    /**
     * The {@link SSLSocket} associated with this SSLConnection
     */
    public final SSLSocket socket;
    /**
     * The {@link PacketInputStream} associated with this SSLConnection
     */
    public final PacketInputStream pis;
    /**
     * The {@link PacketOutputStream} associated with this SSLConnection
     */
    public final PacketOutputStream pos;

    /**
     * Creates a new SSLConnection
     * @param socket The {@link SSLSocket} to associate with this SSLConnection
     * @param isClient Is this SSLConnection being created by a client?
     * @throws IOException If an error occurs initializing the packet streams
     */
    public SSLConnection(SSLSocket socket, boolean isClient) throws IOException {
        this.socket = socket;
        //These have to be initialized in the opposite order so that there isn't a deadlock from the serialization headers
        if(isClient) {
            this.pos = new PacketOutputStream(socket.getOutputStream());
            this.pis = new PacketInputStream(socket.getInputStream());
        } else {
            this.pis = new PacketInputStream(socket.getInputStream());
            this.pos = new PacketOutputStream(socket.getOutputStream());
        }
    }
    
}