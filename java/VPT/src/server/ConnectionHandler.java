package server;

import common.Constants;
import common.networking.packet.Packet;
import common.networking.packet.PacketInputStream;
import common.networking.packet.PacketOutputStream;
import common.networking.packet.packets.ForceLogoutPacket;
import common.networking.packet.packets.ServerStatusPacket;
import common.networking.packet.packets.result.ErrorResultPacket;
import common.networking.ssl.SSLConnection;
import java.io.EOFException;
import java.io.IOException;

/**
 * Manages a {@link SSLConnection}
 * @see PacketHandler
 */
public class ConnectionHandler {
    
    /**
     * The SSLConnection handled by this ConnectionHandler
     */
    private final SSLConnection connection;
    /**
     * The PacketInputStream associated with the SSLConnection
     */
    private final PacketInputStream pis;
    /**
     * The PacketOutputStream associated with the SSLConnection
     */
    private final PacketOutputStream pos;
    /**
     * A reference to {@link #onUserDeletion()}. It is initialized when {@link #doHandleConnection()} is called
     */
    private Runnable onUserDeletion;
    /**
     * Whether {@link #handleConnection()} has been called
     */
    private volatile boolean isRunning;

    /**
     * Creates a new ConnectionHandler and initializes the streams
     * @param connection the SSLConnection to handle
     * @param status a ServerStatusPacket describing the current status of the server
     * @throws IOException if there is an error writing headers to the streams
     */
    public ConnectionHandler(SSLConnection connection, ServerStatusPacket status) throws IOException {
        this.connection = connection;
        pis = connection.pis;
        pos = connection.pos;
        pos.writeDouble(ServerConstants.MIN_SUPPORTED_CLIENT_VERSION);
        pos.writeDouble(ServerConstants.MAX_SUPPORTED_CLIENT_VERSION);
        pos.writeInt(ServerConstants.BRANCH.id);
        pos.writePacket(status);
        isRunning = false;
    }
    
    /**
     * Begins handling of the SSLConnection
     * @throws IllegalStateException if this method has already been called
     * @see #doHandleConnection() 
     */
    public synchronized void handleConnection() throws IllegalStateException {
        if(isRunning) {
            throw new IllegalStateException("Connection is already being handled");
        }
        new Thread(this::doHandleConnection).start();
        isRunning = true;
    }
    
    /**
     * Handles the connection. This should be invoked in a new thread both because
     * it enters a loop and because it needs to run in its own thread for logins to work correctly
     * @see #handleConnection() 
     * @see PacketHandler#process(common.networking.packet.Packet, java.lang.Runnable, common.networking.ssl.SSLConnection) 
     */
    private void doHandleConnection() {
        onUserDeletion = this::onUserDeletion;
        while(!connection.socket.isClosed()) {
            try {
                Packet packet = PacketHandler.process(pis.readPacket(), onUserDeletion, connection);
                if(packet == null) {
                    continue;
                }
                pos.writePacket(packet);
            } catch(IOException e) {
                if(e instanceof EOFException) {
                    try {
                        connection.socket.close();
                    } catch(IOException exc) {}
                }
                if(ServerConstants.BRANCH.id <= Constants.Branch.ALPHA.id && !connection.socket.isClosed()) {
                    e.printStackTrace(System.err);
                }
                try {
                    pos.writePacket(ErrorResultPacket.SERVER_ERROR);
                } catch(IOException exc) {
                    if(ServerConstants.BRANCH.id <= Constants.Branch.ALPHA.id && !connection.socket.isClosed()) {
                        exc.printStackTrace(System.err);
                    }
                }
            }
        }
    }
    
    /**
     * Sends a {@link ForceLogoutPacket} to the client. Called when the currently logged in user is deleted.
     */
    private void onUserDeletion() {
        if(!connection.socket.isClosed()) {
            try {
                pos.writePacket(new ForceLogoutPacket());
            } catch(IOException e) {
                if (ServerConstants.BRANCH.id <= Constants.Branch.ALPHA.id && !connection.socket.isClosed()) {
                    e.printStackTrace(System.err);
                }
            }
        }
    }
    
}