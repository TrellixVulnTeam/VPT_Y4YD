package common.networking.packet;

import common.Constants;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PacketInputStream extends FilterInputStream {
    
    protected final ObjectInputStream ois;
    protected final DigestInputStream digester;
    
    public PacketInputStream(InputStream is) throws IOException, NoSuchAlgorithmException {
        this(new DigestInputStream(is, MessageDigest.getInstance(Constants.HASH_MODE)));
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
        try {
            Object packetObj = ois.readObject();
            digester.on(false);
            Object hashObj = ois.readObject();
            if(packetObj != null && packetObj instanceof Packet && hashObj != null && hashObj instanceof byte[]) {
                if(MessageDigest.isEqual(digester.getMessageDigest().digest(), (byte[])hashObj)) {
                    return (Packet)packetObj;
                }
            }
            return Packet.NULL_PACKET;
        } finally {
            digester.on(true);
        }
    }
    
    public Object readUnhashedObject() throws ClassNotFoundException, IOException {
        try {
            digester.on(false);
            return ois.readObject();
        } finally {
            digester.on(true);
        }
    }
    
    public double readUnhashedDouble() throws IOException {
        try {
            digester.on(false);
            return ois.readDouble();
        } finally {
            digester.on(true);
        }
    }
    
    public byte readUnhashedByte() throws IOException {
        try {
            digester.on(false);
            return ois.readByte();
        } finally {
            digester.on(true);
        }
    }
    
    public boolean readUnhashedBoolean() throws IOException {
        try {
            digester.on(false);
            return ois.readBoolean();
        } finally {
            digester.on(true);
        }
    }
    
    public int readUnhashedInt() throws IOException {
        try {
            digester.on(false);
            return ois.readInt();
        } finally {
            digester.on(true);
        }
    }
    
}