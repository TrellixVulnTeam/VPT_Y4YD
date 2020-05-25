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

/**
 * Serializes objects while backing up the original files in {@link ServerConstants#BACKUP_DIR}
 */
public final class BackupSerialization {
    
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
     * @throws IOException if there was an error serializing the object
     */
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
        
        try {
            
            File file = new File(ServerConstants.SERVER_DIR + fileName);
            file.createNewFile();
            File bkupFile = new File(ServerConstants.BACKUP_DIR + fileName + ".bkup");
            bkupFile.createNewFile();
            Files.copy(file.toPath(), bkupFile.toPath());
            try(ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file))) {
                os.writeObject(o);
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
     * @return The deserialized object
     * @throws ClassNotFoundException if the class of the returned object cannot be found
     * @throws IOException if there was an error deserializing the object
     */
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
        
        try {
        
            Object output;
            File file = new File(ServerConstants.SERVER_DIR);
            try(ObjectInputStream is = new ObjectInputStream(new FileInputStream(file))) {
                output = is.readObject();
            }
            return output;
            
        } finally {
        
            synchronized(locks.get(fileName)) {
                activeFiles.remove(fileName);
                locks.get(fileName).notify();
            }
            
        }
    }
    
    /**
     * Restores a backup file to its original location
     * @param fileName the filename of the file to attempt to restore. This will be assumed relative to {@link ServerConstants#SERVER_DIR}
     * @throws FileNotFoundException if no backup file could be found
     * @throws IOException if there was an error restoring the file
     */
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