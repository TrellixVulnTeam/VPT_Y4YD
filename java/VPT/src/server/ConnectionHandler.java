package server;

import common.Constants;
import common.networking.AESServerConnection;
import common.networking.packet.Packet;
import common.networking.packet.PacketId;
import common.networking.packet.PacketInputStream;
import common.networking.packet.PacketOutputStream;
import common.networking.packet.packets.LoginPacket;
import common.networking.packet.packets.LoginResultPacket;
import common.networking.packet.packets.ServerErrorResult;
import java.io.IOException;
import server.user.LoginService;

public class ConnectionHandler {
    
    private final AESServerConnection connection;
    private final PacketInputStream pis;
    private final PacketOutputStream pos;
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
        while(!connection.isClosed()) {
            try {
                Packet p = pis.readPacket();
                if(p.id == PacketId.LOGIN.id) {
                    LoginPacket loginPacket = (LoginPacket)p;
                    pos.writePacket(new LoginResultPacket(LoginService.login(loginPacket.userId, loginPacket.password)));
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
    
}