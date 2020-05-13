package common.networking.ssl;

import common.networking.packet.PacketInputStream;
import common.networking.packet.PacketOutputStream;
import java.io.IOException;
import javax.net.ssl.SSLSocket;

public class SSLConnection {
    
    public final SSLSocket socket;
    public final PacketInputStream pis;
    public final PacketOutputStream pos;

    public SSLConnection(SSLSocket socket) throws IOException {
        this.socket = socket;
        this.pis = new PacketInputStream(socket.getInputStream());
        this.pos = new PacketOutputStream(socket.getOutputStream());
    }
    
}