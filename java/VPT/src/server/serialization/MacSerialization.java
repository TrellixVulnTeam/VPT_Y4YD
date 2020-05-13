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

public final class MacSerialization {
    
    private static final ArrayList<String> activeFiles = new ArrayList<>();
    private static final HashMap<String, Object> locks = new HashMap<>();
    
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
        
        synchronized(locks.get(fileName)) {
            activeFiles.remove(fileName);
            locks.get(fileName).notify();
        }
    }
    
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
        
        synchronized(locks.get(fileName)) {
            activeFiles.remove(fileName);
            locks.get(fileName).notify();
        }
        return output;
    }
    
    private MacSerialization() {}
    
}