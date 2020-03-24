package common.networking.packet;

import common.Constants;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PacketOutputStream extends FilterOutputStream {
    
    protected final ObjectOutputStream oos;
    protected final DigestOutputStream digester;
    
    public PacketOutputStream(OutputStream os) throws IOException, NoSuchAlgorithmException {
        this(new DigestOutputStream(os, MessageDigest.getInstance(Constants.HASH_MODE)));
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
        } finally {
            digester.on(true);
        }
    }
    
    public void writeUnhashedObject(Object obj) throws IOException {
        try {
            digester.on(false);
            oos.writeObject(obj);
        } finally {
            digester.on(true);
        }
    }
    
    public void writeUnhashedDouble(double d) throws IOException {
        try {
            digester.on(false);
            oos.writeDouble(d);
        } finally {
            digester.on(true);
        }
    }
    
    public void writeUnhashedByte(byte b) throws IOException {
        try {
            digester.on(false);
            oos.writeByte(b);
        } finally {
            digester.on(true);
        }
    }
    
    public void writeUnhashedBoolean(boolean b) throws IOException {
        try {
            digester.on(false);
            oos.writeBoolean(b);
        } finally {
            digester.on(true);
        }
    }
    
    public void writeUnhashedInt(int i) throws IOException {
        try {
            digester.on(false);
            oos.writeInt(i);
        } finally {
            digester.on(true);
        }
    }
    
}