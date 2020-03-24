package server;

import common.networking.AESServer;
import common.networking.AESServerConnection;
import common.networking.AESSocket;
import java.io.IOException;

public final class ServerMain {
    
    public static void main(String[] args) {
        try {
            AESServer server = new AESServer(null);
            while(true) {
                AESServerConnection connection = server.accept();
                if(connection.isConnected() && !connection.isClosed()) {
                    AESSocket socket = new AESSocket(connection);
                    socket.getOutputStream().writeDouble(ServerConstants.MIN_SUPPORTED_CLIENT_VERSION);
                    socket.getOutputStream().writeDouble(ServerConstants.MAX_SUPPORTED_CLIENT_VERSION);
                    socket.getOutputStream().writeInt(ServerConstants.BRANCH.id);
                    new ConnectionHandler(socket).handleConnection();
                }
            }
        } catch(IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private ServerMain() {}
    
}