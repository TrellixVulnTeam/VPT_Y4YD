package server;

import common.networking.packet.Packet;
import common.networking.packet.PacketId;
import common.networking.packet.packets.LoginPacket;
import common.networking.packet.packets.result.DefaultResults;
import common.networking.packet.packets.result.ErrorResultPacket;
import common.networking.packet.packets.result.ResultPacket;
import server.user.LoginService;
import server.user.User;
import server.user.UserStore;

public final class PacketHandler {
    
    public static ResultPacket process(Packet p, Runnable onUserDeletion) {
        try {
            if(p.id == PacketId.LOGIN.id) {
                User currentUser = LoginService.getCurrentUser();
                if(currentUser != null) {
                    LoginService.logout();
                    UserStore.unsubscribeFromDeletionEvents(currentUser.userID, onUserDeletion);
                }
                LoginPacket loginPacket = (LoginPacket)p;
                boolean result = LoginService.login(loginPacket.userId, loginPacket.password);
                UserStore.subscribeToDeletionEvents(loginPacket.userId, onUserDeletion);
                return DefaultResults.login(result);
            }
            return ErrorResultPacket.INVALID_REQUEST;
        } catch(SecurityException e) {
            return ErrorResultPacket.ILLEGAL_ACCESS(e.getMessage());
        }
    }
    
    private PacketHandler() {}
    
}