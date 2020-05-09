package server.user;

import common.Utils;
import common.user.AttributeSearch;
import common.user.AttributeSearchCriteria;
import common.user.NetPublicUser;
import java.io.File;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.lang.ref.WeakReference;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import server.ServerConstants;
import server.serialization.BackupSerialization;
import server.serialization.EncryptionSerialization;
import server.serialization.MacSerialization;

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
    private static final ConcurrentHashMap<String, PublicKey> userPublicKeys = new ConcurrentHashMap<>();
    private static final ReentrantReadWriteLock publicKeyLock = new ReentrantReadWriteLock(ServerConstants.USE_FAIR_LOCKS);
    private static final Consumer<User> saveUser = (user) -> {
        try {
            saveUser(user, false);
        } catch(IOException e) {
            System.err.println("Error saving user: " + user.userId);
            e.printStackTrace(System.err);
        }
    };
    private static boolean attributesChanged = false, publicKeysChanged = false;
    
    public static NetPublicUser getPublicUser(String userId) throws IllegalArgumentException, IOException {
        userLock.readLock().lock();
        try {
            if(!checkUserIdExistance(userId)) {
                throw new IllegalArgumentException("User doesn't exist");
            }
            User user = getDRUserInternal(userId);
            if(user != null) {
                return user.toNetPublicUser();
            }
            publicKeyLock.readLock().lock();
            try {
                return (NetPublicUser)MacSerialization.deserialize("Users/" + Utils.hash(user.userId) + ".usr.pub", userPublicKeys.get(userId));
            } catch(InvalidKeyException | InvalidObjectException e) {
                e.printStackTrace(System.err);
                throw new IOException(e);
            } catch(ClassNotFoundException | IOException e) {
                throw new IOException(e);
            } finally {
                publicKeyLock.readLock().unlock();
            }
        } finally {
            userLock.readLock().unlock();
        }
    }
    
    public static User getUser(String userId, Key decryptionKey) throws SecurityException {
        User user = getUserInternal(userId, decryptionKey);
        LoginService.checkAccess(user);
        return user;
    }
    
    public static User login(String userId, byte[] password) {
        User user = getUserInternal(userId, Utils.createPasswordKey(password));
        return user != null && user.checkPassword(password) ? user : null;
    }
    
    private static User getUserInternal(String userId, Key decryptionKey) {
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
                        user = (User)EncryptionSerialization.deserialize("Users/" + Utils.hash(userId) + ".usr", decryptionKey);
                    } catch(ClassCastException | ClassNotFoundException | IOException | InvalidKeyException e) {
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
    
    public static Key getUserKey(String userId) throws IllegalArgumentException {
        if(!checkUserIdExistance(userId)) {
            throw new IllegalArgumentException("User doesn't exist");
        }
        if(LoginService.getCurrentUser().userId.equals(userId)) {
            return LoginService.getCurrentUser().getKey("USER_FILE_SECRET_KEY");
        } else if(LoginService.getCurrentUser().isAdmin()) {
            //TODO: Implement key fetching
        }
        return null;
    }
    
    private static User getDRUserInternal(String userId) {
        userLock.readLock().lock();
        try {
            if(!users.containsKey(userId) || users.get(userId).get() == null) {
                return null;
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
            if(!checkUserIdExistance(userId)) {
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
        return getDRUserInternal(user.userId) == user;
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
            KeyPair publicFileKeys = Utils.createPseudoRandomAsymetricKey();
            user.setKey("USER_FILE_PRIVATE_KEY", publicFileKeys.getPrivate());
            user.setKey("USER_FILE_PUBLIC_KEY", publicFileKeys.getPublic());
            publicKeyLock.writeLock().lock();
            try {
                userPublicKeys.put(user.userId, publicFileKeys.getPublic());
                publicKeysChanged = true;
            } finally {
                publicKeyLock.writeLock().unlock();
            }
            saveUser(user, true);
            users.put(user.userId, new WeakReference<>(user));
            if(user.isVisible()) {
                attributeLock.writeLock().lock();
                attributesChanged = true;
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
        User user = getUserInternal(userId, getUserKey(userId));
        LoginService.checkAccess(user);
        editedUserLock.writeLock().lock();
        try {
            userLock.writeLock().lock();
            try {
                deletionSubscribersLock.writeLock().lock();
                try {
                    if(!checkUserIdExistance(userId)) {
                        throw new IllegalArgumentException("User Does Not Exist");
                    }
                    editedUsers.remove(userId);
                    new File(ServerConstants.SERVER_DIR + File.separator + "Users" + File.separator + Utils.hash(userId) + ".usr").delete();
                    users.remove(userId);
                    attributeLock.writeLock().lock();
                    try {
                        publicAttributes.remove(userId);
                    } finally {
                        attributeLock.writeLock().unlock();
                    }
                    ArrayList<Runnable> deletionSubs = deletionSubscribers.get(userId);
                    if(deletionSubs != null) {
                        deletionSubs.forEach((sub) -> sub.run());
                    }
                    publicKeyLock.writeLock().lock();
                    try {
                        userPublicKeys.remove(userId);
                        publicKeysChanged = true;
                    } finally {
                        publicKeyLock.writeLock().unlock();
                    }
                } finally {
                    deletionSubscribersLock.writeLock().unlock();
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
        try {
            EncryptionSerialization.serialize(user, "Users/" + Utils.hash(user.userId) + ".usr", user.getKey("USER_FILE_SECRET_KEY"));
            MacSerialization.serialize(user, "Users/" + Utils.hash(user.userId) + ".usr.pub", (PrivateKey)user.getKey("USER_FILE_PRIVATE_KEY"));
        } catch(InvalidKeyException e) {
            throw new IOException(e);
        }
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
            attributesChanged = true;
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
        HashMap<String, ArrayList<UserAttribute>> attributes = (HashMap<String, ArrayList<UserAttribute>>)BackupSerialization.deserialize("Users/attributes.attrs");
        attributeLock.writeLock().lock();
        try {
            publicAttributes.putAll(attributes);
        } finally {
            attributeLock.writeLock().unlock();
        }
    }
    
    public static void saveAttributes() throws IOException {
        if(!attributesChanged) {
            return;
        }
        File attributesFile = new File(ServerConstants.SERVER_DIR + File.separator + "Users" + File.separator + "attributes.attrs");
        attributesFile.createNewFile();
        attributeLock.writeLock().lock();
        try {
            attributesChanged = false;
            BackupSerialization.serialize(getPublicAttributes(), "Users/attributes.attrs");
        } finally {
            attributeLock.writeLock().unlock();
        }
    }
    
    public static void loadPublicKeys() throws ClassNotFoundException, IOException {
        File keyFile = new File(ServerConstants.SERVER_DIR + File.separator + "Users" + File.separator + "publickeys.pks");
        if(!keyFile.exists()) {
            keyFile.createNewFile();
            return;
        }
        if(keyFile.length() == 0) {
            return;
        }
        HashMap<String, PublicKey> keys = (HashMap<String, PublicKey>)BackupSerialization.deserialize("Users/publickeys.pks");
        publicKeyLock.writeLock().lock();
        try {
            userPublicKeys.putAll(keys);
        } finally {
            publicKeyLock.writeLock().unlock();
        }
    }
    
    public static void savePublicKeys() throws IOException {
        if(!publicKeysChanged) {
            return;
        }
        File keyFile = new File(ServerConstants.SERVER_DIR + File.separator + "Users" + File.separator + "publickeys.pks");
        keyFile.createNewFile();
        publicKeyLock.writeLock().lock();
        try {
            publicKeysChanged = false;
            BackupSerialization.serialize(getPublicAttributes(), "Users/publickeys.pks");
        } finally {
            publicKeyLock.writeLock().unlock();
        }
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