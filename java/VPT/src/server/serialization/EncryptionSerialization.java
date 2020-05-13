package server.serialization;

import common.Utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import server.ServerConstants;

public final class EncryptionSerialization {
    
    private static final ArrayList<String> activeFiles = new ArrayList<>();
    private static final HashMap<String, Object> locks = new HashMap<>();
    
    public static void serialize(Serializable o, String fileName, Key encryptionKey) throws InvalidKeyException, IOException {
        fileName = fileName.replaceAll("/", File.separator);
        synchronized(locks) {
            if(!locks.containsKey(fileName)) {
                locks.put(fileName, new Object());
            }
        }
        synchronized(locks.get(fileName)) {
            while(activeFiles.contains(fileName)) {
                try {locks.get(fileName).wait();} catch(InterruptedException e) {}
            }
            activeFiles.add(fileName);
        }
        
        File file = new File(ServerConstants.SERVER_DIR + fileName);
        file.createNewFile();
        File bkupFile = new File(ServerConstants.BACKUP_DIR + fileName + ".bkup");
        bkupFile.createNewFile();
        Files.copy(file.toPath(), bkupFile.toPath());
        Utils.IVCipher cipher = Utils.createCipher(Cipher.ENCRYPT_MODE, encryptionKey);
        DigestOutputStream digester = new DigestOutputStream(new FileOutputStream(file), Utils.createMD());
        byte[] iv = cipher.iv;
        digester.write(Utils.intToBytes(iv.length));
        digester.write(iv);
        try(ObjectOutputStream os = new ObjectOutputStream(new CipherOutputStream(digester, cipher.cipher))) {
            os.writeObject(o);
            digester.on(false);
            os.writeObject(digester.getMessageDigest().digest());
        }
        bkupFile.delete();
        
        synchronized(locks.get(fileName)) {
            activeFiles.remove(fileName);
            locks.get(fileName).notify();
        }
        
    }
    
    public static Object deserialize(String fileName, Key decryptionKey) throws ClassNotFoundException, InvalidKeyException, InvalidObjectException, IOException {
        fileName = fileName.replaceAll("/", File.separator);
        synchronized(locks) {
            if(!locks.containsKey(fileName)) {
                locks.put(fileName, new Object());
            }
        }
        synchronized(locks.get(fileName)) {
            while(activeFiles.contains(fileName)) {
                try {locks.get(fileName).wait();} catch(InterruptedException e) {}
            }
            activeFiles.add(fileName);
        }
        
        Object output;
        File file = new File(ServerConstants.SERVER_DIR);
        DigestInputStream digester = new DigestInputStream(new FileInputStream(file), Utils.createMD());
        try(ObjectInputStream is = new ObjectInputStream(new CipherInputStream(digester,
                Utils.createCipher(Cipher.DECRYPT_MODE, decryptionKey,
                        digester.readNBytes(Utils.bytesToInt(digester.readNBytes(Integer.BYTES)))).cipher))) {
            output = is.readObject();
            digester.on(false);
            try {
                if(MessageDigest.isEqual(digester.getMessageDigest().digest(), (byte[])is.readObject())) {
                    throw new ClassCastException();
                }
            } catch(ClassCastException e) {
                throw new InvalidObjectException("Invalid Hash");
            }
        }
        
        synchronized(locks.get(fileName)) {
            activeFiles.remove(fileName);
            locks.get(fileName).notify();
        }
        return output;
    }
    
    private EncryptionSerialization() {}
    
}