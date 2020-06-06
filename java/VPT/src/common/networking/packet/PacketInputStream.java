package common.networking.packet;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

/**
 * An {@link InputStream} with special methods for reading {@link Packet}s
 */
public class PacketInputStream extends FilterInputStream implements ObjectInput {
    
    /**
     * An internal {@link ObjectInputStream} to use for data reading
     */
    protected final ObjectInputStream ois;
    
    /**
     * Creates a PacketInputStream which reads from the specified {@link InputStream}
     * @param is the InputStream to read from
     * @throws IOException if an error occurs creating the stream
     */
    public PacketInputStream(InputStream is) throws IOException {
        this(new ObjectInputStream(is));
    }
    
    /**
     * Creates a PacketInputStream which reads from the specified {@link ObjectInputStream}
     * @param ois the ObjectInputStream to read from
     */
    public PacketInputStream(ObjectInputStream ois) {
        super(ois);
        this.ois = ois;
    }
    
    /**
     * Reads a {@link Packet} from the stream
     * @return The {@link Packet} read from the stream
     * @throws IOException if an I/O error occurs while reading the stream
     */
    public Packet readPacket() throws IOException {
        try {
            Object packetObj = ois.readObject();
            if(packetObj != null && packetObj instanceof Packet) {
                return (Packet)packetObj;
            }
        } catch(ClassNotFoundException e) {}
        return Packet.NULL_PACKET;
    }

    @Override
    public Object readObject() throws ClassNotFoundException, IOException {
        return ois.readObject();
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        ois.readFully(b);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        ois.readFully(b, off, len);
    }

    @Override
    public int skipBytes(int n) throws IOException {
        return ois.skipBytes(n);
    }

    @Override
    public boolean readBoolean() throws IOException {
        return ois.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return ois.readByte();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return ois.readUnsignedByte();
    }

    @Override
    public short readShort() throws IOException {
        return ois.readShort();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return ois.readUnsignedShort();
    }

    @Override
    public char readChar() throws IOException {
        return ois.readChar();
    }

    @Override
    public int readInt() throws IOException {
        return ois.readInt();
    }

    @Override
    public long readLong() throws IOException {
        return ois.readLong();
    }

    @Override
    public float readFloat() throws IOException {
        return ois.readFloat();
    }

    @Override
    public double readDouble() throws IOException {
        return ois.readDouble();
    }

    @Override
    @Deprecated
    public String readLine() throws IOException {
        return ois.readLine();
    }

    @Override
    public String readUTF() throws IOException {
        return ois.readUTF();
    }
    
}