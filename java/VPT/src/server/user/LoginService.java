package server.user;

public final class LoginService {
    
    private static final ThreadLocal<Session> session = ThreadLocal.withInitial(() -> new Session());
    
    public static boolean login(String userId, byte[] password) {
        User user = UserStore.login(userId, password);
        if(user != null) {
            session.get().setUser(user);
            return true;
        }
        return false;
    }
    
    public static void logout() {
        session.get().setUser(null);
    }
    
    public static User getCurrentUser() {
        return session.get().getUser();
    }
    
    public static void checkAccess() throws SecurityException {
        if(getCurrentUser() != null && getCurrentUser().isAdmin()) {
            return;
        }
        throw new SecurityException("Invalid Permissions");
    }
    
    public static void checkAccess(User user) throws SecurityException {
        if(getCurrentUser() == user) {
            return;
        }
        checkAccess();
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