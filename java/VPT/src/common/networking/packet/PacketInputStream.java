package common.networking.packet;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class PacketInputStream extends FilterInputStream {
    
    protected final ObjectInputStream ois;
    
    public PacketInputStream(ObjectInputStream ois) {
        super(ois);
        this.ois = ois;
    }
    
    public Packet readPacket() throws ClassNotFoundException, IOException {
        Object obj = ois.readObject();
        if(obj != null && obj instanceof Packet) {
            return (Packet)obj;
        }
        return Packet.NULL_PACKET;
    }
    
}