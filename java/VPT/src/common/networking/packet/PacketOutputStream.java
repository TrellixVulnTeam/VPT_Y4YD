package common.networking.packet;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * An {@link OutputStream} with special methods for writing {@link Packet}s
 */
public class PacketOutputStream extends FilterOutputStream implements ObjectOutput {
    
    /**
     * An internal {@link ObjectOutputStream} to use for data writing
     */
    protected final ObjectOutputStream oos;
    
    /**
     * Creates a PacketOutputStream which writes to the specified {@link OutputStream}
     * @param os The OutputStream to write to
     * @throws IOException if an error occurs creating the stream
     */
    public PacketOutputStream(OutputStream os) throws IOException {
        this(new ObjectOutputStream(os));
    }
    
    /**
     * Creates a PacketOutputStream which writes to the specified {@link OutputStream}
     * @param oos the OutputStream to write to
     */
    public PacketOutputStream(ObjectOutputStream oos) {
        super(oos);
        this.oos = oos;
    }
    
    /**
     * Writes a {@link Packet} to the stream
     * @param packet the Packet to write
     * @throws IOException if an I/O error occurs while writing to the stream
     */
    public void writePacket(Packet packet) throws IOException {
        oos.writeObject(packet);
        oos.flush();
    }

    @Override
    public void writeObject(Object obj) throws IOException {
        oos.writeObject(obj);
        oos.flush();
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        oos.writeBoolean(v);
        oos.flush();
    }

    @Override
    public void writeByte(int v) throws IOException {
        oos.writeByte(v);
        oos.flush();
    }

    @Override
    public void writeShort(int v) throws IOException {
        oos.writeShort(v);
        oos.flush();
    }

    @Override
    public void writeChar(int v) throws IOException {
        oos.writeChar(v);
        oos.flush();
    }

    @Override
    public void writeInt(int v) throws IOException {
        oos.writeInt(v);
        oos.flush();
    }

    @Override
    public void writeLong(long v) throws IOException {
        oos.writeLong(v);
        oos.flush();
    }

    @Override
    public void writeFloat(float v) throws IOException {
        oos.writeFloat(v);
        oos.flush();
    }

    @Override
    public void writeDouble(double v) throws IOException {
        oos.writeDouble(v);
        oos.flush();
    }

    @Override
    public void writeBytes(String s) throws IOException {
        oos.writeBytes(s);
        oos.flush();
    }

    @Override
    public void writeChars(String s) throws IOException {
        oos.writeChars(s);
        oos.flush();
    }

    @Override
    public void writeUTF(String s) throws IOException {
        oos.writeUTF(s);
        oos.flush();
    }
    
}