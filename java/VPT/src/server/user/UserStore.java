package server.user;

import common.Utils;
import common.user.AttributeSearch;
import common.user.AttributeSearchCriteria;
import common.user.NetPublicUser;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import server.ServerConstants;
import server.serialization.DefaultSerialization;

public final class UserStore {

    private static final ConcurrentHashMap<String, WeakReference<User>> users = new ConcurrentHashMap<>();
    private static final ReentrantReadWriteLock userLock = new ReentrantReadWriteLock(ServerConstants.USE_FAIR_LOCKS);
    private static final ConcurrentHashMap<String, User> editedUsers = new ConcurrentHashMap<>();
    //Note: Due to the usage of the editedUsers array, this lock will be used differently (read lock to add, write lock to remove)
    private static final ReentrantReadWriteLock editedUserLock = new ReentrantReadWriteLock(ServerConstants.USE_FAIR_LOCKS);
    private static final ConcurrentHashMap<String, ArrayList<UserAttribute>> publicAttributes = new ConcurrentHashMap<>();
    private static final ReentrantReadWriteLock attributeLock = new ReentrantReadWriteLock(ServerConstants.USE_FAIR_LOCKS);
    private static final ConcurrentHashMap<String, ArrayList<Runnable>> deletionSubscribers = new ConcurrentHashMap();
    private static final ReentrantReadWriteLock deletionSubscribersLock = new ReentrantReadWriteLock(ServerConstants.USE_FAIR_LOCKS);
    private static final Consumer<User> saveUser = (user) -> {
        try {
            saveUser(user, false);
        } catch(IOException e) {
            System.err.println("Error saving user: " + user.userId);
            e.printStackTrace(System.err);
        }
    };
    
    public static NetPublicUser getPublicUser(String userId) {
        return getUserInternal(userId).toNetPublicUser();
    }
    
    public static User getUser(String userId) throws SecurityException {
        User user = getUserInternal(userId);
        LoginService.checkAccess(user);
        return user;
    }
    
    public static User login(String userId, byte[] password) {
        User user = getUserInternal(userId);
        return user != null && user.checkPassword(password) ? user : null;
    }
    
    private static User getUserInternal(String userId) {
        userLock.readLock().lock();
        try {
            if(!users.containsKey(userId) || users.get(userId).get() == null) {
                if(!checkUserIdExistance(userId)) {
                    return null;
                }
                userLock.readLock().unlock();
                userLock.writeLock().lock();
                userLock.readLock().lock();
                try {
                    if(!checkUserIdExistance(userId)) {
                        return null;
                    }
                    User user;
                    try {
                        user = (User)DefaultSerialization.deserialize("Users/" + Utils.hash(userId) + ".usr");
                    } catch(ClassCastException | ClassNotFoundException | IOException e) {
                        return null;
                    }
                    if(user == null) {
                        return null;
                    }
                    users.put(userId, new WeakReference<>(user));
                    return user;
                } finally {
                    userLock.writeLock().unlock();
                }
            } else {
                return users.get(userId).get();
            }
        } finally {
            userLock.readLock().unlock();
        }
    }
    
    public static void subscribeToDeletionEvents(String userId, Runnable onUserDeletion) throws IllegalArgumentException {
        deletionSubscribersLock.readLock().lock();
        try {
            if(getUserInternal(userId) == null) {
                throw new IllegalArgumentException("User: " + userId + " does not exist");
            }
            synchronized(deletionSubscribers) {
                if(!deletionSubscribers.containsKey(userId)) {
                    deletionSubscribers.put(userId, new ArrayList<>());
                }
            }
            synchronized(deletionSubscribers.get(userId)) {
                deletionSubscribers.get(userId).add(onUserDeletion);
            }
        } finally {
            deletionSubscribersLock.readLock().unlock();
        }
    }
    
    public static void unsubscribeFromDeletionEvents(String userId, Runnable callback) {
        deletionSubscribersLock.readLock().lock();
        try {
            synchronized(deletionSubscribers) {
                if(!deletionSubscribers.containsKey(userId)) {
                    return;
                }
            }
            synchronized(deletionSubscribers.get(userId)) {
                deletionSubscribers.get(userId).remove(callback);
            }
        } finally {
            deletionSubscribersLock.readLock().unlock();
        }
    }
    
    public static boolean checkUserIdExistance(String userId) {
        userLock.readLock().lock();
        try {
            return new File(ServerConstants.SERVER_DIR + File.separator + "Users" + File.separator + Utils.hash(userId) + ".usr").exists();
        } finally {
            userLock.readLock().unlock();
        }
    }
    
    public static boolean checkUserExistance(User user) {
        if(user == null) {
            return false;
        }
        userLock.readLock().lock();
        try {
            if(!users.contains(user.userId)) {
                return false;
            }
            return users.get(user.userId).get().equals(user);
        } finally {
            userLock.readLock().unlock();
        }
    }
    
    public static void createUser(User user) throws IllegalArgumentException, IOException, SecurityException {
        LoginService.checkAccess();
        if(checkUserIdExistance(user.userId)) {
            throw new IllegalArgumentException("User Already Exists");
        }
        userLock.writeLock().lock();
        try {
            if(checkUserIdExistance(user.userId)) {
                throw new IllegalArgumentException("User Already Exists");
            }
            saveUser(user, true);
            users.put(user.userId, new WeakReference<>(user));
            if(user.isVisible()) {
                attributeLock.writeLock().lock();
                try {
                    publicAttributes.put(user.userId, user.getAttributes());
                } finally {
                    attributeLock.writeLock().unlock();
                }
            }
        } finally {
            userLock.writeLock().unlock();
        }
    }
    
    public static void deleteUser(String userId) throws IllegalArgumentException, IOException, SecurityException {
        if(!checkUserIdExistance(userId)) {
            throw new IllegalArgumentException("User Does Not Exist");
        }
        User user = getUserInternal(userId);
        LoginService.checkAccess(user);
        editedUserLock.writeLock().lock();
        try {
            editedUsers.remove(userId);
            userLock.writeLock().lock();
            try {
                if(!checkUserIdExistance(userId)) {
                    throw new IllegalArgumentException("User Does Not Exist");
                }
                new File(ServerConstants.SERVER_DIR + File.separator + "Users" + File.separator + Utils.hash(userId) + ".usr").delete();
                users.remove(userId);
                attributeLock.writeLock().lock();
                try {
                    publicAttributes.remove(user.userId);
                } finally {
                    attributeLock.writeLock().unlock();
                }
            } finally {
                userLock.writeLock().unlock();
            }
        } finally {
            editedUserLock.writeLock().unlock();
        }
    }
    
    public static void notifyUserChange(User user) {
        if(!checkUserExistance(user)) {
            return;
        }
        editedUserLock.readLock().lock();
        try {
            if(!editedUsers.contains(user)) {
                editedUsers.put(user.userId, user);
            }
        } finally {
            editedUserLock.readLock().unlock();
        }
    }
    
    public static void saveUsers() {
        editedUserLock.writeLock().lock();
        try {
            editedUsers.values().forEach(saveUser);
            editedUsers.clear();
        } finally {
            editedUserLock.writeLock().unlock();
        }
    }
    
    private static void saveUser(User user, boolean ignoreExistance) throws IOException {
        if(!checkUserExistance(user) && !ignoreExistance) {
            return;
        }
        DefaultSerialization.serialize(user, "Users/" + Utils.hash(user.userId) + ".usr");
    }
    
    @SuppressWarnings("LockAcquiredButNotSafelyReleased")
    public static void notifyVisibilityChange(User user) {
        if(!checkUserExistance(user)) {
            return;
        }
        attributeLock.readLock().lock();
        try {
            boolean isVisible = user.isVisible();
            if(publicAttributes.containsKey(user.userId) == isVisible) {
                return;
            }
            attributeLock.readLock().unlock();
            attributeLock.writeLock().lock();
            attributeLock.readLock().lock();
            try {
                if(publicAttributes.containsKey(user.userId) == isVisible) {
                    return;
                }
                if(isVisible) {
                    publicAttributes.put(user.userId, user.getAttributes());
                } else {
                    publicAttributes.remove(user.userId);
                }
            } finally {
                attributeLock.writeLock().unlock();
            }
        } finally {
            attributeLock.readLock().unlock();
        }
    }
    
    public static void loadAttributes() throws ClassNotFoundException, IOException {
        File attributesFile = new File(ServerConstants.SERVER_DIR + File.separator + "Users" + File.separator + "attributes.attrs");
        if(!attributesFile.exists()) {
            attributesFile.createNewFile();
            return;
        }
        if(attributesFile.length() == 0) {
            return;
        }
        HashMap<String, ArrayList<UserAttribute>> attributes = (HashMap<String, ArrayList<UserAttribute>>)DefaultSerialization.deserialize("Users/attributes.attrs");
        attributeLock.writeLock().lock();
        try {
            publicAttributes.putAll(attributes);
        } finally {
            attributeLock.writeLock().unlock();
        }
    }
    
    public static void saveAttributes() throws IOException {
        File attributesFile = new File(ServerConstants.SERVER_DIR + File.separator + "Users" + File.separator + "attributes.attrs");
        attributesFile.createNewFile();
        DefaultSerialization.serialize(getPublicAttributes(), "Users/attributes.attrs");
    }
    
    public static HashMap<String, ArrayList<UserAttribute>> getPublicAttributes() {
        attributeLock.readLock().lock();
        try {
            return new HashMap<>(publicAttributes);
        } finally {
            attributeLock.readLock().unlock();
        }
    }
    
    public static ArrayList<String> search(AttributeSearch search) {
        HashMap<String, ArrayList<UserAttribute>> attributes = getPublicAttributes();
        ArrayList<AttributeSearchCriteria> criteria = search.getCriteria();
        HashMap<String, Integer> rankings = new HashMap<>();
        attributes.forEach((userId, userAttributes) -> {
            int rank = 0;
            boolean elegable = true;
            for(AttributeSearchCriteria crit: criteria) {
                if(!elegable) {
                    return;
                }
                switch(crit.location) {
                    case ANYWHERE:
                        for(UserAttribute attribute: userAttributes) {
                            int count = attribute.search(crit);
                            if(crit.isBlacklist) {
                                elegable = !(!elegable || count > 0);
                            } else {
                                rank += count;
                            }
                        }
                    case USERID:
                        int countUID = Utils.countStringMatches(userId, crit.search, crit.quote, crit.matchCase);
                        if(crit.isBlacklist) {
                            elegable = !(!elegable || countUID > 0);
                        } else {
                            rank += countUID;
                        }
                        break;
                    default:
                        if(!crit.location.isIrregular) {
                            for(UserAttribute attribute: userAttributes) {
                                if(attribute.type == crit.location.equivilentType) {
                                    int count = attribute.search(crit);
                                    if(crit.isBlacklist) {
                                        elegable = !(!elegable || count > 0);
                                    } else {
                                        rank += count;
                                    }
                                }
                            }
                        }
                        break;
                }
            }
            if(elegable && rank > 0) {
                rankings.put(userId, rank);
            }
        });
        ArrayList<String> out = new ArrayList<>(rankings.keySet());
        out.sort((a, b) -> rankings.get(a) - rankings.get(b));
        return out;
    }
    
    private UserStore() {}

}