package server;

import common.Console;
import common.Constants;
import common.networking.packet.PacketId;
import common.networking.packet.packets.ServerStatus;
import common.networking.packet.packets.SingleDataPacket;
import common.networking.ssl.SSLConfig;
import common.networking.ssl.SSLConnection;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import server.user.LoginService;
import server.user.UserStore;

/**
 * Provides an entry point for the server side of the VPT
 */
public final class ServerMain {
    
    /**
     * A ScheduledThreadPoolExecutor used for executing periodic methods
     */
    private static final ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(1);
    
    /**
     * The entry point for the server side of the VPT
     * @param args the command line arguments
     */
    @SuppressWarnings("UseSpecificCatch")
    public static void main(String[] args) {
        @SuppressWarnings("UnusedAssignment")
        Console console = null;
        if(ServerConstants.BRANCH == Constants.Branch.DEV) {
            console = new Console(true, true, true);
        } else {
            try {
                console = new Console(true, false, true);
            } catch(IllegalArgumentException e) {
                System.err.println("Error: No Console Detected");
                System.exit(1);
            }
        }
        createDirs();
        try {
            try {
                char[] sslKeystorePassword = console.readPassword("Enter SSL Keystore Password: ");
                KeyStore sslKeystore = KeyStore.getInstance(new File(ServerConstants.SERVER_DIR + File.separator + "SSLKeystore.keystore"), sslKeystorePassword);
                SSLConfig.initServer(sslKeystore, sslKeystorePassword, "sslkey");
            } catch(CertificateException | IOException | KeyStoreException | NoSuchAlgorithmException e) {
                throw new Exception("Error Loading SSL Keystore", e);
            } catch(KeyManagementException e) {
                throw new Exception("Error Initializing SSLConfig", e);
            }
        } catch(Exception e) {
            System.err.println(e.getMessage());
            e.getCause().printStackTrace(System.err);
            System.exit(1);
        }
        loadData();
        try {
            startPeriodicMethods();
            try {
                SSLServerSocket server = SSLConfig.createServerSocket(ServerConstants.SERVER_PORT);
                while(true) {
                    try {
                        SSLConnection connection = new SSLConnection((SSLSocket)server.accept(), false);
                        if(connection.socket.isConnected() && !connection.socket.isClosed()) {
                            SingleDataPacket<ServerStatus> status = new SingleDataPacket<>(PacketId.SERVER_STATUS, ServerStatus.OK);
                            ConnectionHandler handler = new ConnectionHandler(connection, status);
                            if(status.data == ServerStatus.OK) {
                                handler.handleConnection();
                            } else {
                                connection.socket.close();
                            }
                        }
                    } catch(IOException e) {
                        if(ServerConstants.BRANCH.id <= Constants.Branch.ALPHA.id) {
                            e.printStackTrace(System.err);
                        }
                    }
                }
            } catch(IOException e) {
                e.printStackTrace(System.err);
            }
        } finally {
            executor.shutdown();
        }
    }
    
    /**
     * Creates the server directories
     */
    private static void createDirs() {
        createDir("");
        createDir("Users");
    }
    
    /**
     * Creates the requested directory and a corresponding backup directory
     * @param dir the directory file path. This will be assumed relative to {@link ServerConstants#SERVER_DIR}
     * and {@link ServerConstants#BACKUP_DIR} for the working and backup directories respectively
     */
    private static void createDir(String dir) {
        new File(ServerConstants.SERVER_DIR + File.separator + dir.replace("/", File.separator)).mkdirs();
        new File(ServerConstants.BACKUP_DIR + File.separator + dir.replace("/", File.separator)).mkdirs();
    }
    
    /**
     * Initializes {@link #executor} to run {@link #doPeriodic()} every {@link ServerConstants#PERIODIC_INTERVAL} nanoseconds
     * and registers {@link #saveData()} as a shutdown hook
     */
    private static void startPeriodicMethods() {
        executor.scheduleWithFixedDelay(ServerMain::doPeriodic, ServerConstants.PERIODIC_INTERVAL, ServerConstants.PERIODIC_INTERVAL, TimeUnit.NANOSECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(ServerMain::saveData));
    }
    
    /**
     * Runs periodic methods. These will save modified data and cleanup any old data
     */
    private static void doPeriodic() {
        RequestService.cleanup();
        saveData();
    }
    
    /**
     * Loads required data from disk into memory. This should only run at the start of the program
     */
    private static void loadData() {
        try {
            UserStore.loadAdminKey();
            UserStore.loadAttributes();
            UserStore.loadPublicKeys();
        } catch(ClassNotFoundException | IOException e) {
            System.err.println("Error loading data");
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * Saves any modified data to disk
     */
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