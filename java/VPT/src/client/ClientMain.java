package client;

import common.Constants;
import common.networking.packet.Packet;
import common.networking.packet.PacketId;
import common.networking.packet.PacketInputStream;
import common.networking.packet.PacketOutputStream;
import common.networking.packet.packets.ServerStatus;
import common.networking.packet.packets.SingleDataPacket;
import common.networking.ssl.SSLConfig;
import common.networking.ssl.SSLConnection;
import java.awt.GraphicsEnvironment;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLSocket;
import javax.swing.JOptionPane;

/**
 * Provides an entry point for the client side of the VPT
 */
public final class ClientMain {
    
    /**
     * A SSLConnection binding the {@link #socket} containing the connection to its corresponding packet streams
     */
    private static SSLConnection connection = null;
    /**
     * The SSLSocket holding the client's connection to the server
     */
    private static SSLSocket socket = null;
    /**
     * The PacketInputStream used to receive data from the server
     */
    private static PacketInputStream pis = null;
    /**
     * The PacketOutputStream used to send data from the server
     */
    private static PacketOutputStream pos = null;
    /**
     * Controls whether the native client code is notified when the {@link #socket} is closed. This ensures that the native code is not notified of a close it initiated.
     */
    private static boolean doNativeSocketCloseNotify = true;
    
    /**
     * The entry point for the client side of the VPT
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if(GraphicsEnvironment.isHeadless()) {
            System.err.println("Cannot run in headless enviornment");
            System.exit(1);
        }
        try {
            KeyStore truststore = KeyStore.getInstance(new File("C:\\VPT\\truststore.keystore"), "VPTtrst".toCharArray());
            SSLConfig.initClient(truststore);
        } catch(CertificateException | IOException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException e) {
            handleStartupError("Error Initializing SSL Truststore", e);
        }
        try {
            socket = SSLConfig.createSocket(ClientConstants.SERVER_IP, ClientConstants.SERVER_PORT);
        } catch(IOException e) {
            handleStartupError("Error Connecting to Server", e);
        }
        try {
            connection = new SSLConnection(socket, true);
            socket = connection.socket;
            pis = connection.pis;
            pos = connection.pos;
            double minClientVersion = pis.readDouble();
            double maxClientVersion = pis.readDouble();
            int serverBranchId = pis.readInt();
            SingleDataPacket<ServerStatus> serverStatusPacket = (SingleDataPacket<ServerStatus>)pis.readPacket();
            checkConnection(minClientVersion, maxClientVersion, serverBranchId, serverStatusPacket);
        } catch(ClassCastException e) {
            handleStartupError("Invalid Server Status", e);
        } catch(IOException e) {
            handleStartupError("Error Initializing Connection", e);
        }
        Thread recieveThread = new Thread(() -> {
            while(socket.isClosed()) {
                try {
                    Packet packet = pis.readPacket();
                    if(packet == null || packet.id == PacketId.NULL.id) {
                        continue;
                    }
                    ClientJNI.recievePacket(packet);
                } catch(Exception e) {
                    if(e instanceof EOFException) {
                        try {
                            socket.close();
                            break;
                        } catch(IOException exc) {}
                    }
                    if(socket.isClosed()) {
                        if(doNativeSocketCloseNotify) {
                            ClientJNI.socketClosed();
                        }
                        break;
                    } else {
                        if(ClientConstants.BRANCH.id <= Constants.Branch.ALPHA.id) {
                            e.printStackTrace(System.err);
                        }
                    }
                }
            }
        });
        recieveThread.setDaemon(true);
        recieveThread.start();
        ClientJNI.cppMain(args);
    }
    
    /**
     * Sends a packet to to the server
     * @param packet the Packet to send to the server
     * @throws IOException if an error occurs sending the packet
     */
    public static void sendPacket(Packet packet) throws IOException {
        pos.writePacket(packet);
    }
    
    /**
     * Closes the connection to the server
     * @throws IOException if an error occurs closing the connection
     */
    public static void closeSocket() throws IOException {
        doNativeSocketCloseNotify = false;
        socket.close();
    }
    
    /**
     * Notifies the user of an error while connecting to the server and terminates the program
     * @param error a message describing the error
     * @param e the exception causing the error
     */
    private static void handleStartupError(String error, Exception e) {
        if(ClientConstants.BRANCH.id <= Constants.Branch.ALPHA.id) {
            System.err.println(error + ": ");
            e.printStackTrace(System.err);
        } else {
            JOptionPane.showMessageDialog(null, error, "VPT", JOptionPane.ERROR_MESSAGE);
        }
        System.exit(1);
    }

    /**
     * Checks that the client and server are of compatible versions and branches and notifies the user if there is an error
     * @param minClientVersion the server's minimum supported client version
     * @param maxClientVersion the server's maximum supported client version
     * @param serverBranchId the {@link Constants#Branch#id} of the {@link Constants#Branch} of the server
     * @param serverStatusPacket a ServerStatusPacket representing the status of the server
     */
    private static void checkConnection(double minClientVersion, double maxClientVersion, int serverBranchId, SingleDataPacket<ServerStatus> serverStatusPacket) {
        boolean isClientDev = ClientConstants.BRANCH.id <= Constants.Branch.ALPHA.id;
        boolean isServerDev = serverBranchId <= Constants.Branch.ALPHA.id;
        if(isServerDev && !isClientDev) {
            int shouldContinue = JOptionPane.showConfirmDialog(null, "WARNING: This Server is in Development Mode, Would You Like To Connect?", "VPT", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (shouldContinue != JOptionPane.YES_OPTION) {
                System.exit(1);
            }
        }
        if(minClientVersion > ClientConstants.VERSION) {
            if(!isServerDev && !isClientDev) {
                JOptionPane.showMessageDialog(null, "Your Client Is Outdated; Cannot Connect", "VPT", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            } else {
                int shouldContinue = JOptionPane.showConfirmDialog(null, "Your Client Is Outdated, Would You Like To Connect?", "VPT", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if(shouldContinue != JOptionPane.YES_OPTION) {
                    System.exit(1);
                }
            }
        } else if(maxClientVersion < ClientConstants.VERSION) {
            if(!isServerDev && !isClientDev) {
                JOptionPane.showMessageDialog(null, "Server Is Outdated; Cannot Connect", "VPT", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            } else {
                int shouldContinue = JOptionPane.showConfirmDialog(null, "This Server Is Outdated, Would You Like To Connect?", "VPT", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if(shouldContinue != JOptionPane.YES_OPTION) {
                    System.exit(1);
                }
            }
        }
        switch(serverStatusPacket.data) {
            case OFFLINE:
                JOptionPane.showMessageDialog(null, "Error: Server Offline", "VPT", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
                break;
            case TOO_MANY_REQUESTS:
                JOptionPane.showMessageDialog(null, "Error: Too Many Requests, Try again later.", "VPT", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
                break;
            case OK:
            default:
                break;
        }
    }
    
    private ClientMain() {}
    
}