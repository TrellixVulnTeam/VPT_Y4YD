package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

public final class BackupSerialization {
    
    private static final ArrayList<String> activeFiles = new ArrayList<>();
    private static final HashMap<String, Object> locks = new HashMap<>();
    
    public static void backupAndSerialize(Object o, String fileName, Function<File, ObjectOutputStream> osFunction) throws IOException {
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
        File bkupFile = new File(ServerConstants.BACKUP_DIR + fileName + ".bkup");
        bkupFile.createNewFile();
        Files.copy(file.toPath(), bkupFile.toPath());
        try(ObjectOutputStream os = osFunction.apply(file)) {
            os.writeObject(o);
        }
        bkupFile.delete();
        
        synchronized(locks.get(fileName)) {
            activeFiles.remove(fileName);
            locks.get(fileName).notify();
        }
        
    }
    
    public static synchronized void restore(String fileName) throws FileNotFoundException, IOException {
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