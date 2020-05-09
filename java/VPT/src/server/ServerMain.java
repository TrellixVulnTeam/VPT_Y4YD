package server;

import common.Constants;
import common.networking.AESServer;
import common.networking.AESServerConnection;
import common.networking.packet.packets.ServerStatus;
import common.networking.packet.packets.ServerStatusPacket;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import server.user.LoginService;
import server.user.UserStore;

public final class ServerMain {
    
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    
    public static void main(String[] args) {
        createDirs();
        loadData();
        startPeriodicMethods();
        try {
            AESServer server = new AESServer(null);
            while(true) {
                try {
                    AESServerConnection connection = server.accept();
                    if(connection.isConnected() && !connection.isClosed()) {
                        ServerStatusPacket status = new ServerStatusPacket(ServerStatus.OK);
                        ConnectionHandler handler = new ConnectionHandler(connection, status);
                        if(status.data == ServerStatus.OK) {
                            handler.handleConnection();
                        } else {
                            connection.close();
                        }
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
    
    private static void startPeriodicMethods() {
        executor.scheduleWithFixedDelay(ServerMain::doPeriodic, ServerConstants.PERIODIC_INTERVAL, ServerConstants.PERIODIC_INTERVAL, TimeUnit.NANOSECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(ServerMain::saveData));
    }
    
    private static void doPeriodic() {
        RequestService.cleanup();
        saveData();
    }
    
    private static void loadData() {
        try {
            UserStore.loadAttributes();
            UserStore.loadPublicKeys();
        } catch(ClassNotFoundException | IOException e) {
            System.err.println("Error loading data");
            e.printStackTrace(System.err);
        }
    }
    
    private static void saveData() {
        LoginService.markAsSystemThread();
        try {
            UserStore.saveUsers();
            UserStore.saveAttributes();
            UserStore.savePublicKeys();
        } catch(IOException e) {
            System.err.println("Error saving data");
            e.printStackTrace(System.err);
        }
    }

    private ServerMain() {}
    
}