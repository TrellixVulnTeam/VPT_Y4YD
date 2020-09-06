package server;

import common.UserDataField;
import server.services.RequestService;
import common.networking.packet.Packet;
import common.networking.packet.PacketId;
import common.networking.packet.packets.CreateUserPacket;
import common.networking.packet.packets.CurrentUserDataRequestPacket;
import common.networking.packet.packets.DeleteUserPacket;
import common.networking.packet.packets.LoginPacket;
import common.networking.packet.packets.result.DoubleResultPacket;
import common.networking.packet.packets.result.ErrorResultPacket;
import common.networking.packet.packets.result.ResultPacket;
import common.networking.packet.packets.result.ResultType;
import common.networking.packet.packets.result.StandardResultPacket;
import common.networking.ssl.SSLConnection;
import java.sql.SQLException;
import java.time.Duration;
import server.services.LoginService;
import server.services.UserService;

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
                int currentUser = LoginService.getCurrentUserId();
                if(currentUser != -1) {
                    LoginService.logout();
                    UserService.unsubscribeFromDeletionEvents(currentUser, onUserDeletion);
                }
                LoginPacket loginPacket = (LoginPacket)p;
                boolean result = LoginService.login(loginPacket.username, loginPacket.password);
                if(result) {
                    UserService.subscribeToDeletionEvents(LoginService.getCurrentUserId(), onUserDeletion);
                }
                return new StandardResultPacket(result, result ? null : "Invalid Login");
            } else if(p.id == PacketId.CREATE_USER.id) {
                RequestService.request(connection, "Create User", ServerConstants.USER_SPEC_REQUESTS_TE);
                try {
                    CreateUserPacket packet = (CreateUserPacket)p;
                    UserService.createUser(packet.userId, packet.password);
                    return new StandardResultPacket(true);
                } catch(IllegalArgumentException e) {
                    return new StandardResultPacket(false, e.getMessage());
                }
            } else if(p.id == PacketId.DELETE_USER.id) {
                RequestService.request(connection, "Delete User", ServerConstants.USER_ONET_REQUESTS_TE);
                try {
                    DeleteUserPacket packet = (DeleteUserPacket)p;
                    UserService.deleteUser(packet.data);
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
                RequestService.request(connection, "CurrentUserRequest", ServerConstants.AUTO_REQUESTS_TE);
                if(LoginService.getCurrentUserId() == -1) {
                    throw new SecurityException("You Are Not Logged In");
                }
                UserDataField requestField = ((CurrentUserDataRequestPacket)p).data;
                Object result;
                switch(requestField) {
                    case USERNAME:
                        result = UserService.getUsername(LoginService.getCurrentUserId());
                        break;
                    case USERICON:
                        result = UserService.getUserIcon(LoginService.getCurrentUserId());
                        break;
                    case NULL:
                    default:
                        result = null;
                }
                return new DoubleResultPacket<>(ResultType.CURRENT_USER_RESULT, true, null, requestField.id, result);
            }
            return ErrorResultPacket.INVALID_REQUEST;
        } catch(RequestService.TooManyRequestsException e) {
            return ErrorResultPacket.TOO_MANY_REQUESTS(formatTimeout(e.timeout));
        } catch(SecurityException e) {
            return ErrorResultPacket.ILLEGAL_ACCESS(e.getMessage());
        } catch(ClassCastException e) {
            return ErrorResultPacket.INVALID_REQUEST;
        } catch(SQLException e) {
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