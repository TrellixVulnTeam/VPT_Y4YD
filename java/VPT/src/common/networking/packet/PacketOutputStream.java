package common.networking.packet;

import common.Utils;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;

public class PacketOutputStream extends FilterOutputStream {
    
    protected final ObjectOutputStream oos;
    protected final DigestOutputStream digester;
    
    public PacketOutputStream(OutputStream os) throws IOException {
        this(new DigestOutputStream(os, Utils.createMD()));
    }
    
    private PacketOutputStream(DigestOutputStream digester) throws IOException {
        this(new ObjectOutputStream(digester), digester);
    }
    
    private PacketOutputStream(ObjectOutputStream oos, DigestOutputStream digester) {
        super(oos);
        this.oos = oos;
        this.digester = digester;
    }
    
    public void writePacket(Packet packet) throws IOException {
        try {
            oos.writeObject(packet);
            digester.on(false);
            oos.writeObject(digester.getMessageDigest().digest());
            oos.flush();
        } finally {
            digester.on(true);
        }
    }
    
    public void writeUnhashedObject(Object obj) throws IOException {
        try {
            digester.on(false);
            oos.writeObject(obj);
            oos.flush();
        } finally {
            digester.on(true);
        }
    }
    
    public void writeUnhashedDouble(double d) throws IOException {
        try {
            digester.on(false);
            oos.writeDouble(d);
            oos.flush();
        } finally {
            digester.on(true);
        }
    }
    
    public void writeUnhashedByte(byte b) throws IOException {
        try {
            digester.on(false);
            oos.writeByte(b);
            oos.flush();
        } finally {
            digester.on(true);
        }
    }
    
    public void writeUnhashedBoolean(boolean b) throws IOException {
        try {
            digester.on(false);
            oos.writeBoolean(b);
            oos.flush();
        } finally {
            digester.on(true);
        }
    }
    
    public void writeUnhashedInt(int i) throws IOException {
        try {
            digester.on(false);
            oos.writeInt(i);
            oos.flush();
        } finally {
            digester.on(true);
        }
    }
    
}