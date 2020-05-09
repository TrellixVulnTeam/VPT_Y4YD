package server.serialization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import server.ServerConstants;

public final class BackupSerialization {
    
    private static final ArrayList<String> activeFiles = new ArrayList<>();
    private static final HashMap<String, Object> locks = new HashMap<>();
    
    public static void serialize(Serializable o, String fileName) throws IOException {
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
        try(ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file))) {
            os.writeObject(o);
        }
        bkupFile.delete();
        
        synchronized(locks.get(fileName)) {
            activeFiles.remove(fileName);
            locks.get(fileName).notify();
        }
        
    }
    
    public static Object deserialize(String fileName) throws ClassNotFoundException, IOException {
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
        try(ObjectInputStream is = new ObjectInputStream(new FileInputStream(file))) {
            output = is.readObject();
        }
        
        synchronized(locks.get(fileName)) {
            activeFiles.remove(fileName);
            locks.get(fileName).notify();
        }
        return output;
    }
    
    public static synchronized void restore(String fileName) throws FileNotFoundException, IOException {
        fileName = fileName.replaceAll("/", File.separator);
        File file = new File(ServerConstants.SERVER_DIR + fileName);
        File bkupFile = new File(ServerConstants.BACKUP_DIR + fileName + ".bkup");
        if(!bkupFile.exists()) {
            throw new FileNotFoundException("File: " + bkupFile.getAbsolutePath() + " does not exist");
        }
        file.createNewFile();
        Files.copy(bkupFile.toPath(), file.toPath());
        bkupFile.delete();
    }
    
    private BackupSerialization() {}
    
}