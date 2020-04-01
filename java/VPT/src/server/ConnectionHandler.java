package server;

import common.Constants;
import common.networking.AESServerConnection;
import common.networking.packet.Packet;
import common.networking.packet.PacketId;
import common.networking.packet.PacketInputStream;
import common.networking.packet.PacketOutputStream;
import common.networking.packet.packets.ForceLogoutPacket;
import common.networking.packet.packets.result.IllegalAccessPacket;
import common.networking.packet.packets.LoginPacket;
import common.networking.packet.packets.result.LoginResultPacket;
import common.networking.packet.packets.result.ServerErrorResult;
import java.io.IOException;
import server.user.LoginService;
import server.user.User;
import server.user.UserStore;

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
                try {
                    Packet p = pis.readPacket();
                    if(p.id == PacketId.LOGIN.id) {
                        User currentUser = LoginService.getCurrentUser();
                        if(currentUser != null) {
                            LoginService.logout();
                            UserStore.unsubscribeFromDeletionEvents(currentUser.userID, onUserDeletion);
                        }
                        LoginPacket loginPacket = (LoginPacket)p;
                        boolean result = LoginService.login(loginPacket.userId, loginPacket.password);
                        UserStore.subscribeToDeletionEvents(loginPacket.userId, onUserDeletion);
                        pos.writePacket(new LoginResultPacket(result));
                    }
                } catch(SecurityException e) {
                    pos.writePacket(new IllegalAccessPacket());
                }
            } catch(ClassCastException e) {
            } catch(ClassNotFoundException | IOException e) {
                if(ServerConstants.BRANCH.id <= Constants.Branch.ALPHA.id && !connection.isClosed()) {
                    e.printStackTrace(System.err);
                }
                try {
                    pos.writePacket(new ServerErrorResult());
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