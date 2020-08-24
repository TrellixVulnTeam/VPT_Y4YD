package server.user;

import server.services.LoginService;
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
import java.util.regex.Pattern;
import server.ServerConstants;
import server.serialization.BackupSerialization;
import server.serialization.EncryptionSerialization;
import server.serialization.MacSerialization;

/**
 * Stores users and their related attributes
 */
public final class UserStore {
    //Note: Due to the usage of the some maps, some locks will be used differently (read lock to add, write lock to remove)

    /**
     * Contains references to all current users
     */
    private static final ConcurrentHashMap<String, WeakReference<User>> users = new ConcurrentHashMap<>();
    /**
     * Protects access to {@link #users}
     */
    private static final ReentrantReadWriteLock userLock = new ReentrantReadWriteLock(ServerConstants.USE_FAIR_LOCKS);
    /**
     * Contains references to all users with edited data
     */
    private static final ConcurrentHashMap<String, User> editedUsers = new ConcurrentHashMap<>();
    /**
     * Protects access to {@link #editedUsers}
     */
    private static final ReentrantReadWriteLock editedUserLock = new ReentrantReadWriteLock(ServerConstants.USE_FAIR_LOCKS);
    /**
     * Contains all publicly available attributes of users
     */
    private static final ConcurrentHashMap<String, ArrayList<UserAttribute>> publicAttributes = new ConcurrentHashMap<>();
    /**
     * Protects access to {@link #publicAttributes}
     */
    private static final ReentrantReadWriteLock attributeLock = new ReentrantReadWriteLock(ServerConstants.USE_FAIR_LOCKS);
    /**
     * Contains methods to be run on the deletion of users
     */
    private static final ConcurrentHashMap<String, ArrayList<Runnable>> deletionSubscribers = new ConcurrentHashMap<>();
    /**
     * Protects access to {@link #deletionSubscribers}
     */
    private static final ReentrantReadWriteLock deletionSubscribersLock = new ReentrantReadWriteLock(ServerConstants.USE_FAIR_LOCKS);
    /**
     * Contains the public keys of users
     */
    private static final ConcurrentHashMap<String, PublicKey> userPublicKeys = new ConcurrentHashMap<>();
    /**
     * Protects access to {@link #userPublicKeys}
     */
    private static final ReentrantReadWriteLock publicKeyLock = new ReentrantReadWriteLock(ServerConstants.USE_FAIR_LOCKS);
    /**
     * Represents the admin public key
     */
    private static PublicKey ADMIN_PUBLIC_KEY;
    /**
     * A {@link Consumer} of users which saves them
     */
    private static final Consumer<User> saveUser = (user) -> {
        try {
            saveUser(user, false);
        } catch(IOException e) {
            System.err.println("Error saving user: " + user.userId);
            e.printStackTrace(System.err);
        }
    };
    /**
     * Whether {@link #publicAttributes} has been edited
     */
    private static boolean attributesChanged = false;
    /**
     * Whether {@link #userPublicKeys} has been edited
     */
    private static boolean publicKeysChanged = false;
    
    /**
     * Retrieves a NetPublicUser containing the data for the specified userId
     * @param userId the userId of the user to retrieve
     * @return a NetPublicUser containing the data for the specified userId
     * @throws IllegalArgumentException if the specified userId does not exist
     * @throws IOException if there was an error reading the user's data
     */
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
                return (NetPublicUser)MacSerialization.deserialize("Users/" + Utils.hash(userId) + ".usr.pub", userPublicKeys.get(userId));
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
    
    /**
     * Retrieves the user with the specified userId or <code>null</code> if none could be found or there was an error in retrieving them
     * @param userId the userId of the user to retrieve
     * @param decryptionKey the key to use to read the user file if necessary
     * @return the user with the specified userId
     * @throws SecurityException if the current user does not have the required permissions to access the requested user
     * @see #getUserInternal(java.lang.String, java.security.Key) 
     */
    public static User getUser(String userId, Key decryptionKey) throws SecurityException {
        User user = getUserInternal(userId, decryptionKey);
        LoginService.checkAccess(user);
        return user;
    }
    
    /**
     * Retrieves the user with the specified userId and checks the given password against it
     * @param userId the userId of the user to retrieve
     * @param password the password of the requested user
     * @return the user with the specified userId or <code>null</code> if the password is incorrect
     */
    public static User login(String userId, byte[] password) {
        User user = getUserInternal(userId, Utils.createPasswordKey(password));
        return user != null && user.checkPassword(password) ? user : null;
    }
    
    /**
     * Retrieves the user with the specified userId or <code>null</code> if the user could not be found or if an error occurred in retrieving them
     * @param userId the userId of the user to retrieve
     * @param decryptionKey the key to use to read the user file if necessary
     * @return the user with the specified userId
     * @see #getDRUserInternal(java.lang.String) 
     * @see #getUser(java.lang.String, java.security.Key) 
     * @see #login(java.lang.String, byte[]) 
     */
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
    
    /**
     * Retrieves a user's secret key or <code>null</code> if the current user does not have the permissions to access it
     * @param userId the userId to retrieve the key for
     * @return the user's secret key
     * @throws IllegalArgumentException if the specified user does not exist
     */
    public static Key getUserKey(String userId) throws IllegalArgumentException {
        if(!checkUserIdExistance(userId)) {
            throw new IllegalArgumentException("User doesn't exist");
        }
        if(LoginService.getCurrentUser().userId.equals(userId)) {
            return LoginService.getCurrentUser().getKey("USER_FILE_SECRET_KEY");
        } else if(LoginService.getCurrentUser().isAdmin()) {
            try {
                return (Key)EncryptionSerialization.deserialize("Users/" + Utils.hash(userId), LoginService.getCurrentUser().getKey("ADMIN_PRIVATE_KEY"));
            } catch(ClassNotFoundException | InvalidKeyException | IOException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * Retrieves a currently loaded user or <code>null</code> if no currently loaded user could be found with that id
     * @param userId the userId of the user to retrieve
     * @return the currently loaded user with the specified id
     */
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
    
    /**
     * Registers the given method to be run if the specified user is deleted
     * @param userId the userId of the user to check
     * @param onUserDeletion the method to be run
     * @throws IllegalArgumentException if the specified user does not exist
     */
    public static void subscribeToDeletionEvents(String userId, Runnable onUserDeletion) throws IllegalArgumentException {
        deletionSubscribersLock.readLock().lock();
        try {
            userLock.readLock().lock();
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
                userLock.readLock().unlock();
            }
        } finally {
            deletionSubscribersLock.readLock().unlock();
        }
    }
    
    /**
     * Unregisters the given method from being run when the specified user is deleted
     * @param userId the userId of the user being checked
     * @param callback the method which would be run
     */
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
    
    /**
     * Checks if the given userId corresponds to an existing user
     * @param userId the userId to check
     * @return whether the given userId corresponds to an existing user
     */
    public static boolean checkUserIdExistance(String userId) {
        userLock.readLock().lock();
        try {
            return new File(ServerConstants.SERVER_DIR + File.separator + "Users" + File.separator + Utils.hash(userId) + ".usr").exists();
        } finally {
            userLock.readLock().unlock();
        }
    }
    
    /**
     * Checks if the given user is registered
     * @param user the user to check
     * @return whether the given user is registered
     */
    public static boolean checkUserExistance(User user) {
        if(user == null) {
            return false;
        }
        return getDRUserInternal(user.userId) == user;
    }
    
    /**
     * Registers the given user and creates all required files
     * @param user the user to create
     * @throws IllegalArgumentException if the userId of the given user is invalid
     * @throws IOException if there is an error creating any of the required files
     * @throws SecurityException if the current user does not have the permissions to create the specified user
     */
    public static void createUser(User user) throws IllegalArgumentException, IOException, SecurityException {
        if(isInvalidUserId(user.userId)) {
            throw new IllegalArgumentException("User Id Cannot Contain Any of the Following Characters: " + ServerConstants.USERID_FORBIDDEN_CHARACTERS);
        }
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
            try {
                EncryptionSerialization.serialize(user.getKey("USER_FILE_SECRET_KEY"), "Users/" + Utils.hash(user.userId) + ".usr.key", ADMIN_PUBLIC_KEY);
            } catch(InvalidKeyException e) {
                throw new IOException(e);
            }
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
    
    /**
     * Deletes the specified user
     * @param userId the userId of the user to delete
     * @throws IllegalArgumentException if the specified user does not exist
     * @throws IOException if there is an error deleting the user's files
     * @throws SecurityException if the current user does not have the required permissions to delete the specified user
     */
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
    
    /**
     * Marks the given user as modified
     * @param user the user to mark
     */
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
    
    /**
     * Saves all modified users
     */
    public static void saveUsers() {
        editedUserLock.writeLock().lock();
        try {
            editedUsers.values().forEach(saveUser);
            editedUsers.clear();
        } finally {
            editedUserLock.writeLock().unlock();
        }
    }
    
    /**
     * Saves a user
     * @param user the user to save
     * @param ignoreExistance whether the user's registration status will be ignored.
     * If this flag is set, the method will not check if the user exists in {@link #users}
     * @throws IOException if there is an error saving the user
     */
    private static void saveUser(User user, boolean ignoreExistance) throws IOException {
        if(!ignoreExistance && !checkUserExistance(user)) {
            return;
        }
        try {
            EncryptionSerialization.serialize(user, "Users/" + Utils.hash(user.userId) + ".usr", user.getKey("USER_FILE_SECRET_KEY"));
            MacSerialization.serialize(user.toNetPublicUser(), "Users/" + Utils.hash(user.userId) + ".usr.pub", (PrivateKey)user.getKey("USER_FILE_PRIVATE_KEY"));
        } catch(InvalidKeyException e) {
            throw new IOException(e);
        }
    }
    
    /**
     * Notifies that the visibility status of the specified user has changed
     * @param user the user whose visibility status has changed
     */
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
    
    /**
     * Populates {@link #publicAttributes}
     * @throws ClassNotFoundException if the read data's class cannot be found
     * @throws IOException if an error occurs reading the data
     */
    public static void loadAttributes() throws ClassNotFoundException, IOException {
        File attributesFile = new File(ServerConstants.SERVER_DIR + File.separator + "Users" + File.separator + "attributes.attrs");
        if(!attributesFile.exists()) {
            attributesFile.createNewFile();
            return;
        }
        if(attributesFile.length() == 0) {
            return;
        }
        @SuppressWarnings("unchecked")
        HashMap<String, ArrayList<UserAttribute>> attributes = (HashMap<String, ArrayList<UserAttribute>>)BackupSerialization.deserialize("Users/attributes.attrs");
        attributeLock.writeLock().lock();
        try {
            publicAttributes.putAll(attributes);
        } finally {
            attributeLock.writeLock().unlock();
        }
    }
    
    /**
     * Saves {@link #publicAttributes}
     * @throws IOException if an error occurs saving the data
     */
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
    
    /**
     * Populates {@link #userPublicKeys}
     * @throws ClassNotFoundException if the read data's class cannot be found
     * @throws IOException if an error occurs reading the data
     */
    public static void loadPublicKeys() throws ClassNotFoundException, IOException {
        File keyFile = new File(ServerConstants.SERVER_DIR + File.separator + "Users" + File.separator + "publickeys.pks");
        if(!keyFile.exists()) {
            keyFile.createNewFile();
            return;
        }
        if(keyFile.length() == 0) {
            return;
        }
        @SuppressWarnings("unchecked")
        HashMap<String, PublicKey> keys = (HashMap<String, PublicKey>)BackupSerialization.deserialize("Users/publickeys.pks");
        publicKeyLock.writeLock().lock();
        try {
            userPublicKeys.putAll(keys);
        } finally {
            publicKeyLock.writeLock().unlock();
        }
    }
    
    /**
     * Saves {@link #userPublicKeys}
     * @throws IOException if an error occurs saving the data
     */
    public static void savePublicKeys() throws IOException {
        if(!publicKeysChanged) {
            return;
        }
        File keyFile = new File(ServerConstants.SERVER_DIR + File.separator + "Users" + File.separator + "publickeys.pks");
        keyFile.createNewFile();
        publicKeyLock.writeLock().lock();
        try {
            publicKeysChanged = false;
            BackupSerialization.serialize(new HashMap<>(userPublicKeys), "Users/publickeys.pks");
        } finally {
            publicKeyLock.writeLock().unlock();
        }
    }
    
    /**
     * Retrieves all publicly available user attributes
     * @return all publicly available user attributes
     * @see #search(common.user.AttributeSearch) 
     */
    public static HashMap<String, ArrayList<UserAttribute>> getPublicAttributes() {
        attributeLock.readLock().lock();
        try {
            return new HashMap<>(publicAttributes);
        } finally {
            attributeLock.readLock().unlock();
        }
    }
    
    public static void loadAdminKey() throws IOException, ClassNotFoundException {
        ADMIN_PUBLIC_KEY = (PublicKey)BackupSerialization.deserialize("Users/ADMIN_PUBLIC.key");
    }
    
    /**
     * Searches all publicly available attributes for the specified criteria
     * @param search the search criteria
     * @return a ranked list of users fitting the criteria
     * @see #getPublicAttributes() 
     */
    @SuppressWarnings("fallthrough")
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
    
    
    /**
     * A pattern which matches to any invalid userId
     */
    public static final Pattern INVALID_USERID_PATTERN = Pattern.compile(ServerConstants.USERID_FORBIDDEN_CHARACTERS_REGEX);
    /**
     * Checks if the given userId is invalid
     * @param userId the userId to check
     * @return whether the given userId is invalid
     */
    public static boolean isInvalidUserId(String userId) {
        return INVALID_USERID_PATTERN.matcher(userId).find();
    }
    
    
    private UserStore() {}

}