package server.serialization;

import common.Utils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;
import server.ServerConstants;

public final class MacSerialization {
    
    private static final ArrayList<String> activeFiles = new ArrayList<>();
    private static final HashMap<String, Object> locks = new HashMap<>();
    
    public static void serialize(Object o, String fileName, Function<File, OutputStream> osFunction) throws IOException {
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
        DigestOutputStream digester = new DigestOutputStream(osFunction.apply(file), Utils.createMD());
        try(ObjectOutputStream os = new ObjectOutputStream(digester)) {
            os.writeObject(o);
            digester.on(false);
            os.writeObject(digester.getMessageDigest().digest());
        }
        
        synchronized(locks.get(fileName)) {
            activeFiles.remove(fileName);
            locks.get(fileName).notify();
        }
    }
    
    public static Object deserialize(String fileName, Function<File, InputStream> isFunction) throws ClassNotFoundException, InvalidObjectException, IOException {
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
        DigestInputStream digester = new DigestInputStream(isFunction.apply(file), Utils.createMD());
        try(ObjectInputStream is = new ObjectInputStream(digester)) {
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
    
    private MacSerialization() {}
    
}