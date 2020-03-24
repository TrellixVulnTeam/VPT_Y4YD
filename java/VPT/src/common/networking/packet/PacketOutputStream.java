package common.networking.packet;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class PacketOutputStream extends FilterOutputStream {
    
    protected final ObjectOutputStream oos;
    
    public PacketOutputStream(ObjectOutputStream oos) {
        super(oos);
        this.oos = oos;
    }
    
    public void writePacket(Packet packet) throws IOException {
        oos.writeObject(packet);
    }
    
}