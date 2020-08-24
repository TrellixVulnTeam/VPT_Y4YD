package server;

import server.services.RequestService;
import common.networking.packet.Packet;
import common.networking.packet.PacketId;
import common.networking.packet.packets.CreateUserPacket;
import common.networking.packet.packets.DeleteUserPacket;
import common.networking.packet.packets.LoginPacket;
import common.networking.packet.packets.result.ErrorResultPacket;
import common.networking.packet.packets.result.ResultPacket;
import common.networking.packet.packets.result.ResultType;
import common.networking.packet.packets.result.SingleResultPacket;
import common.networking.packet.packets.result.StandardResultPacket;
import common.networking.ssl.SSLConnection;
import common.user.NetPublicUser;
import java.io.IOException;
import java.time.Duration;
import server.services.LoginService;
import server.user.PublicUser;
import server.user.User;
import server.user.UserStore;

/**
 * Generates {@link ResultPacket}s in response to client requests
 * @see ConnectionHandler
 */
public final class PacketHandler {
    
    /**
     * Processes a client request
     * @param p the Packet request from the client
     * @param onUserDeletion a method to be run if the currently logged in user is logged out
     * @param connection the SSLConnection associated with the client
     * @return a {@link ResultPacket} responding to the client's request
     */
    public static ResultPacket process(Packet p, Runnable onUserDeletion, SSLConnection connection) {
        try {
            if(p == null || p.id == PacketId.NULL.id) {
                return null;
            }
            if(p.id == PacketId.LOGIN.id) {
                RequestService.request(connection, "Login", ServerConstants.USER_SPEC_REQUESTS_TE);
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
                return new StandardResultPacket(result, result ? null : "Invalid Login");
            } else if(p.id == PacketId.CREATE_USER.id) {
                RequestService.request(connection, "Create User", ServerConstants.USER_SPEC_REQUESTS_TE);
                try {
                    CreateUserPacket packet = (CreateUserPacket)p;
                    UserStore.createUser(new User(packet.userId, packet.password, packet.isAdmin));
                    return new StandardResultPacket(true);
                } catch(IllegalArgumentException e) {
                    return new StandardResultPacket(false, e.getMessage());
                }
            } else if(p.id == PacketId.DELETE_USER.id) {
                RequestService.request(connection, "Delete User", ServerConstants.USER_ONET_REQUESTS_TE);
                try {
                    DeleteUserPacket packet = (DeleteUserPacket)p;
                    UserStore.deleteUser(packet.data);
                    return new StandardResultPacket(true);
                } catch(IllegalArgumentException e) {
                    return new StandardResultPacket(false, e.getMessage());
                }
            } else if(p.id == PacketId.SHUTDOWN.id) {
                RequestService.request(connection, "Shutdown", ServerConstants.USER_ONET_REQUESTS_TE);
                System.exit(0);
            } else if(p.id == PacketId.LOGOUT.id) {
                RequestService.request(connection, "Logout", ServerConstants.USER_ONET_REQUESTS_TE);
                LoginService.logout();
                return new StandardResultPacket(true);
            } else if(p.id == PacketId.CURRENT_USER_REQUEST.id) {
                RequestService.request(connection, "Current User Info", ServerConstants.USER_NORM_REQUESTS_TE);
                User currentUser = LoginService.getCurrentUser();
                if(currentUser == null) {
                    return new SingleResultPacket<NetPublicUser>(ResultType.USER_RESULT, false, "Not Logged In", null);
                }
                return new SingleResultPacket<>(ResultType.USER_RESULT, true, null, currentUser.toNetPublicUser());
            }
            return ErrorResultPacket.INVALID_REQUEST;
        } catch(RequestService.TooManyRequestsException e) {
            return ErrorResultPacket.TOO_MANY_REQUESTS(formatTimeout(e.timeout));
        } catch(SecurityException e) {
            return ErrorResultPacket.ILLEGAL_ACCESS(e.getMessage());
        } catch(ClassCastException e) {
            return ErrorResultPacket.INVALID_REQUEST;
        } catch(IOException e) {
            return ErrorResultPacket.SERVER_ERROR;
        }
    }
    
    /**
     * Formats a timeout caused by too many client requests
     * @param timeout the timeout in nanoseconds
     * @return a String representing the timeout in more conventional units
     */
    public static String formatTimeout(long timeout) {
        Duration duration = Duration.ofNanos(timeout);
        long days = duration.toDaysPart();
        int hours = duration.toHoursPart();
        int minutes = duration.toMinutesPart();
        int seconds = duration.toSecondsPart();
        StringBuilder out = new StringBuilder();
        if(days != 0) {
            out.append(days);
            out.append(" Days ");
        }
        if(hours != 0) {
            out.append(hours);
            out.append(" Hours ");
        }
        if(minutes != 0) {
            out.append(minutes);
            out.append(" Minutes ");
        }
        if(seconds != 0) {
            out.append(seconds);
            out.append(" Seconds ");
        }
        return out.toString().trim();
    }
    
    private PacketHandler() {}
    
}