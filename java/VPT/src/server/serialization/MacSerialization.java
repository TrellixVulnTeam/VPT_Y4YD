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
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.SignedObject;
import java.util.ArrayList;
import java.util.HashMap;
import server.ServerConstants;

/**
 * Wraps objects in {@link SignedObject}s and serializes them while backing up the original files in {@link ServerConstants#BACKUP_DIR}
 */
public final class MacSerialization {
    
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
     * @param signingKey the key to use to sign the object
     * @throws InvalidKeyException if the key is invalid
     * @throws IOException if there was an error serializing the object
     */
    public static void serialize(Serializable o, String fileName, PrivateKey signingKey) throws InvalidKeyException, IOException {
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
        
        try {
        
            SignedObject signedObject;
            try {
                signedObject = new SignedObject(o, signingKey, Utils.createSignature());
            } catch(SignatureException e) {
                throw new IOException(e);
            }
            File file = new File(ServerConstants.SERVER_DIR + fileName);
            file.createNewFile();
            try(ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file))) {
                os.writeObject(signedObject);
            }
            
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
     * @param verificationKey the key to use to verify the object's integrity
     * @return The deserialized object
     * @throws ClassNotFoundException if the class of the returned object cannot be found
     * @throws InvalidKeyException if the provided key is invalid
     * @throws InvalidObjectException if there is an error verifying the integrity of the object
     * @throws IOException if there was an error deserializing the object
     */
    public static Object deserialize(String fileName, PublicKey verificationKey) throws ClassNotFoundException, InvalidKeyException, InvalidObjectException, IOException {
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
        
        try {
        
            Object output;
            File file = new File(ServerConstants.SERVER_DIR + fileName);
            try(ObjectInputStream is = new ObjectInputStream(new FileInputStream(file))) {
                try {
                    SignedObject signedObject = (SignedObject)is.readObject();
                    if(signedObject.verify(verificationKey, Utils.createSignature())) {
                        throw new ClassCastException();
                    }
                    output = signedObject.getObject();
                } catch(SignatureException e) {
                    throw new IOException(e);
                } catch(ClassCastException e) {
                    throw new InvalidObjectException("Invalid Signature");
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
    
    private MacSerialization() {}
    
}