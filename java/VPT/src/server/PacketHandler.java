package server;

import common.Utils;
import common.networking.packet.Packet;
import common.networking.packet.PacketId;
import common.networking.packet.packets.CreateUserPacket;
import common.networking.packet.packets.DeleteUserPacket;
import common.networking.packet.packets.LoginPacket;
import common.networking.packet.packets.result.DefaultResults;
import common.networking.packet.packets.result.ErrorResultPacket;
import common.networking.packet.packets.result.ResultPacket;
import common.networking.ssl.SSLConnection;
import java.io.IOException;
import static server.ServerConstants.USER_REQUESTS_TE;
import static server.ServerConstants.USER_SPEC_REQUESTS_TE;
import server.user.LoginService;
import server.user.User;
import server.user.UserStore;

public final class PacketHandler {
    
    public static ResultPacket process(Packet p, Runnable onUserDeletion, SSLConnection connection) {
        try {
            if(p.id == PacketId.LOGIN.id) {
                RequestService.request(connection, "Login", USER_REQUESTS_TE);
                User currentUser = LoginService.getCurrentUser();
                if(currentUser != null) {
                    LoginService.logout();
                    UserStore.unsubscribeFromDeletionEvents(currentUser.userId, onUserDeletion);
                }
                LoginPacket loginPacket = (LoginPacket)p;
                boolean result = LoginService.login(loginPacket.userId, loginPacket.password);
                if(result) {
                    UserStore.subscribeToDeletionEvents(loginPacket.userId, onUserDeletion);
                }
                return DefaultResults.login(result);
            } else if(p.id == PacketId.CREATE_USER.id) {
                RequestService.request(connection, "Create User", USER_REQUESTS_TE);
                try {
                    CreateUserPacket packet = (CreateUserPacket)p;
                    UserStore.createUser(new User(packet.userId, packet.password, packet.isAdmin));
                    return DefaultResults.createUser(true);
                } catch(IllegalArgumentException e) {
                    return DefaultResults.createUser(false, e.getMessage());
                }
            } else if(p.id == PacketId.DELETE_USER.id) {
                RequestService.request(connection, "Delete User", USER_SPEC_REQUESTS_TE);
                try {
                    DeleteUserPacket packet = (DeleteUserPacket)p;
                    UserStore.deleteUser(packet.data);
                    return DefaultResults.deleteUser(true);
                } catch(IllegalArgumentException e) {
                    return DefaultResults.deleteUser(false, e.getMessage());
                }
            } else if(p.id == PacketId.SHUTDOWN.id) {
                System.exit(0);
            }
            return ErrorResultPacket.INVALID_REQUEST;
        } catch(RequestService.TooManyRequestsException e) {
            return ErrorResultPacket.TOO_MANY_REQUESTS(Utils.formatTimeout(e.timeout));
        } catch(SecurityException e) {
            return ErrorResultPacket.ILLEGAL_ACCESS(e.getMessage());
        } catch(ClassCastException e) {
            return ErrorResultPacket.INVALID_REQUEST;
        } catch(IOException e) {
            return ErrorResultPacket.SERVER_ERROR;
        }
    }
    
    private PacketHandler() {}
    
}