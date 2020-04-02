package server;

import common.Constants;
import common.networking.AESServerConnection;
import common.networking.packet.PacketInputStream;
import common.networking.packet.PacketOutputStream;
import common.networking.packet.packets.ForceLogoutPacket;
import common.networking.packet.packets.result.ErrorResultPacket;
import java.io.IOException;

public class ConnectionHandler {
    
    private final AESServerConnection connection;
    private final PacketInputStream pis;
    private final PacketOutputStream pos;
    private Runnable onUserDeletion;
    private volatile boolean isRunning;

    public ConnectionHandler(AESServerConnection connection) throws IOException {
        this.connection = connection;
        pis = new PacketInputStream(connection.getInputStream());
        pos = new PacketOutputStream(connection.getOutputStream());
        pos.writeUnhashedDouble(ServerConstants.MIN_SUPPORTED_CLIENT_VERSION);
        pos.writeUnhashedDouble(ServerConstants.MAX_SUPPORTED_CLIENT_VERSION);
        pos.writeUnhashedInt(ServerConstants.BRANCH.id);
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
        while(!connection.isClosed()) {
            try {
                pos.writePacket(PacketHandler.process(pis.readPacket(), onUserDeletion));
            } catch(ClassCastException e) {
            } catch(ClassNotFoundException | IOException e) {
                if(ServerConstants.BRANCH.id <= Constants.Branch.ALPHA.id && !connection.isClosed()) {
                    e.printStackTrace(System.err);
                }
                try {
                    pos.writePacket(ErrorResultPacket.SERVER_ERROR);
                } catch(IOException exc) {
                    if(ServerConstants.BRANCH.id <= Constants.Branch.ALPHA.id && !connection.isClosed()) {
                        exc.printStackTrace(System.err);
                    }
                }
            }
        }
    }
    
    private void onUserDeletion() {
        if(!connection.isClosed()) {
            try {
                pos.writePacket(new ForceLogoutPacket());
            } catch(IOException e) {
                if (ServerConstants.BRANCH.id <= Constants.Branch.ALPHA.id && !connection.isClosed()) {
                    e.printStackTrace(System.err);
                }
            }
        }
    }
    
}