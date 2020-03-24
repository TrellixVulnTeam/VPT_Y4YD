package server;

import common.Constants;
import common.networking.AESServer;
import common.networking.AESServerConnection;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public final class ServerMain {
    
    public static void main(String[] args) {
        try {
            AESServer server = new AESServer(null);
            while(true) {
                try {
                    AESServerConnection connection = server.accept();
                    if(connection.isConnected() && !connection.isClosed()) {
                        new ConnectionHandler(connection).handleConnection();
                    }
                } catch(IOException | NoSuchAlgorithmException e) {
                    if(ServerConstants.BRANCH.id <= Constants.Branch.ALPHA.id)
                        e.printStackTrace(System.err);
                }
            }
        } catch(IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private ServerMain() {}
    
}