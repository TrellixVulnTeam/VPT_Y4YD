package common.networking.packet;

import common.Utils;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public class PacketInputStream extends FilterInputStream implements ObjectInput {
    
    protected final ObjectInputStream ois;
    protected final DigestInputStream digester;
    
    public PacketInputStream(InputStream is) throws IOException {
        this(new DigestInputStream(is, Utils.createMD()));
    }
    
    private PacketInputStream(DigestInputStream digester) throws IOException {
        this(new ObjectInputStream(digester), digester);
    }
    
    private PacketInputStream(ObjectInputStream ois, DigestInputStream digester) {
        super(ois);
        this.ois = ois;
        this.digester = digester;
    }
    
    public Packet readPacket() throws ClassNotFoundException, IOException {
        Object packetObj = ois.readObject();
        if(packetObj != null && packetObj instanceof Packet) {
            return (Packet)packetObj;
        }
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
    public String readLine() throws IOException {
        return ois.readLine();
    }

    @Override
    public String readUTF() throws IOException {
        return ois.readUTF();
    }
    
}