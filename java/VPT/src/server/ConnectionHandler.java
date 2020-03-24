package server;

import common.Constants;
import common.networking.AESSocket;
import common.networking.packet.Packet;
import common.networking.packet.PacketInputStream;
import common.networking.packet.PacketOutputStream;
import java.io.IOException;

public class ConnectionHandler {
    
    private final AESSocket socket;
    private final PacketInputStream pis;
    private final PacketOutputStream pos;
    private volatile boolean isRunning;

    public ConnectionHandler(AESSocket socket) {
        this.socket = socket;
        pis = new PacketInputStream(socket.getInputStream());
        pos = new PacketOutputStream(socket.getOutputStream());
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
        while(!socket.getInterface().isClosed()) {
            try {
                Packet p = pis.readPacket();
                //Proccess Packet
            } catch(ClassNotFoundException | IOException e) {
                if(ServerConstants.BRANCH.id <= Constants.Branch.ALPHA.id && !socket.getInterface().isClosed()) {
                    e.printStackTrace();
                }
            }
        }
    }
    
}