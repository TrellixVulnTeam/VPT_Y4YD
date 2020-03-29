package server.user;

import java.io.File;
import server.ServerConstants;

public final class LoginService {
    
    private static final ThreadLocal<Session> session = ThreadLocal.withInitial(() -> new Session());
    
    public static boolean login(String userId, byte[] password) {
        return false;
    }
    
    public static void logout() {
        session.get().setUser(null);
    }
    
    public static User getCurrentUser() {
        return session.get().getUser();
    }
    
    private static final class Session {
        
        private User user;

        private Session() {
            user = null;
        }
        
        private void setUser(User user) {
            this.user = user;
        }
        
        private User getUser() {
            return user;
        }
        
    }
    
}