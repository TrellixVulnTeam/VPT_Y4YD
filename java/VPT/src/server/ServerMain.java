package server;

import common.Constants;
import common.networking.AESServer;
import common.networking.AESServerConnection;
import java.io.File;
import java.io.IOException;

public final class ServerMain {
    
    public static void main(String[] args) {
        createDirs();
        try {
            AESServer server = new AESServer(null);
            while(true) {
                try {
                    AESServerConnection connection = server.accept();
                    if(connection.isConnected() && !connection.isClosed()) {
                        new ConnectionHandler(connection).handleConnection();
                    }
                } catch(IOException e) {
                    if(ServerConstants.BRANCH.id <= Constants.Branch.ALPHA.id)
                        e.printStackTrace(System.err);
                }
            }
        } catch(IOException e) {
            e.printStackTrace(System.err);
        }
    }
    
    private static void createDirs() {
        createDir("");
        createDir("Users");
    }
    
    private static void createDir(String dir) {
        new File(ServerConstants.SERVER_DIR + File.separator + dir.replaceAll("/", File.separator)).mkdirs();
        new File(ServerConstants.BACKUP_DIR + File.separator + dir.replaceAll("/", File.separator)).mkdirs();
    }

    private ServerMain() {}
    
}