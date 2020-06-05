package client;

import common.Constants;
import common.networking.packet.Packet;
import common.networking.packet.PacketId;
import common.networking.packet.PacketInputStream;
import common.networking.packet.PacketOutputStream;
import common.networking.packet.packets.ServerStatusPacket;
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

public final class ClientMain {
    
    private static SSLConnection connection = null;
    private static SSLSocket socket = null;
    private static PacketInputStream pis = null;
    private static PacketOutputStream pos = null;
    
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
            ServerStatusPacket serverStatusPacket = (ServerStatusPacket)pis.readPacket();
            checkConnection(minClientVersion, maxClientVersion, serverBranchId, serverStatusPacket);
        } catch(ClassCastException e) {
            handleStartupError("Invalid Server Status", e);
        } catch(IOException e) {
            handleStartupError("Error Initializing Connection", e);
        }
        Thread recieveThread = new Thread(() -> {
            while(socket.isClosed()) {
                try {
                    ClientJNI.recievePacket(pis.readPacket());
                } catch(Exception e) {
                    if(e instanceof EOFException) {
                        try {
                            socket.close();
                        } catch(IOException exc) {}
                    }
                    if(socket.isClosed()) {
                        ClientJNI.socketClosed();
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
        ClientJNI.main(args);
    }
    
    public static void sendPacket(Packet p) throws IOException {
        pos.writePacket(p);
    }
    
    public static void closeSocket() throws IOException {
        socket.close();
    }
    
    private static void handleStartupError(String error, Exception e) {
        if(ClientConstants.BRANCH.id <= Constants.Branch.ALPHA.id) {
            System.err.println(error + ": ");
            e.printStackTrace(System.err);
        } else {
            JOptionPane.showMessageDialog(null, error, "VPT", JOptionPane.ERROR_MESSAGE);
        }
        System.exit(1);
    }

    private static void checkConnection(double minClientVersion, double maxClientVersion, int serverBranchId, ServerStatusPacket serverStatusPacket) {
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