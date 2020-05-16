package server;

import common.Constants;
import common.networking.packet.PacketInputStream;
import common.networking.packet.PacketOutputStream;
import common.networking.packet.packets.ForceLogoutPacket;
import common.networking.packet.packets.ServerStatusPacket;
import common.networking.packet.packets.result.ErrorResultPacket;
import common.networking.ssl.SSLConnection;
import java.io.EOFException;
import java.io.IOException;

public class ConnectionHandler {
    
    private final SSLConnection connection;
    private final PacketInputStream pis;
    private final PacketOutputStream pos;
    private Runnable onUserDeletion;
    private volatile boolean isRunning;

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
    
    public synchronized void handleConnection() throws IllegalStateException {
        if(isRunning) {
            throw new IllegalStateException("Connection is already handled");
        }
        new Thread(this::doHandleConnection).start();
        isRunning = true;
    }
    
    private void doHandleConnection() {
        onUserDeletion = this::onUserDeletion;
        while(!connection.socket.isClosed()) {
            try {
                pos.writePacket(PacketHandler.process(pis.readPacket(), onUserDeletion, connection));
            } catch(ClassNotFoundException | IOException e) {
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