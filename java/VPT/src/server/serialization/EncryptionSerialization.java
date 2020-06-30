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
import java.nio.file.StandardCopyOption;
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

/**
 * Serializes objects while encrypting them and backing up the original files in {@link ServerConstants#BACKUP_DIR}
 */
public final class EncryptionSerialization {
    
    /**
     * Stores the filenames of files which are currently being modified
     */
    private static final ArrayList<String> activeFiles = new ArrayList<>();
    /**
     * Stores the locks for individual files
     */
    private static final HashMap<String, Object> locks = new HashMap<>();
    
    /**
     * Serializes an object while backing up the original file in {@link ServerConstants#BACKUP_DIR}
     * @param o the object to serialize
     * @param fileName the filename to backup to. This will be assumed relative to {@link ServerConstants#SERVER_DIR}
     * @param encryptionKey the key to use to encrypt the object
     * @throws InvalidKeyException if the key is invalid
     * @throws IOException if there was an error serializing the object
     */
    public static void serialize(Serializable o, String fileName, Key encryptionKey) throws InvalidKeyException, IOException {
        fileName = fileName.replace("/", File.separator);
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
        
        try {
        
            File file = new File(ServerConstants.SERVER_DIR + File.separator + fileName);
            file.createNewFile();
            File bkupFile = new File(ServerConstants.BACKUP_DIR + File.separator + fileName + ".bkup");
            bkupFile.createNewFile();
            Files.copy(file.toPath(), bkupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Cipher cipher = Utils.createCipher(Cipher.ENCRYPT_MODE, encryptionKey);
            DigestOutputStream digester = new DigestOutputStream(new FileOutputStream(file), Utils.createMD());
            try(ObjectOutputStream os = new ObjectOutputStream(new CipherOutputStream(digester, cipher))) {
                os.writeObject(o);
                digester.on(false);
                os.writeObject(digester.getMessageDigest().digest());
            }
            bkupFile.delete();
            
        } finally {

            synchronized(locks.get(fileName)) {
                activeFiles.remove(fileName);
                locks.get(fileName).notify();
            }
        
        }
    }
    
    /**
     * Deserializes an object
     * @param fileName the filename of the file to restore from. This will be assumed relative to {@link ServerConstants#SERVER_DIR}
     * @param decryptionKey the key to use to decrypt the object
     * @return The deserialized object
     * @throws ClassNotFoundException if the class of the returned object cannot be found
     * @throws InvalidKeyException if the provided key is invalid
     * @throws InvalidObjectException if there is an error verifying the integrity of the object
     * @throws IOException if there was an error deserializing the object
     */
    public static Object deserialize(String fileName, Key decryptionKey) throws ClassNotFoundException, InvalidKeyException, InvalidObjectException, IOException {
        fileName = fileName.replace("/", File.separator);
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
        
        try {
        
            Object output;
            File file = new File(ServerConstants.SERVER_DIR + File.separator + fileName);
            DigestInputStream digester = new DigestInputStream(new FileInputStream(file), Utils.createMD());
            try(ObjectInputStream is = new ObjectInputStream(new CipherInputStream(digester,
                    Utils.createCipher(Cipher.DECRYPT_MODE, decryptionKey)))) {
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
            return output;
            
        } finally {

            synchronized(locks.get(fileName)) {
                activeFiles.remove(fileName);
                locks.get(fileName).notify();
            }
            
        }
    }
    
    private EncryptionSerialization() {}
    
}