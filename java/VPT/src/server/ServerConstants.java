package server;

import common.Constants.Branch;
import common.Utils;
import static java.io.File.separator;
import java.util.concurrent.TimeUnit;

/**
 * Constants specific to the server side of the VPT
 */
public final class ServerConstants {
    
    //VERSION
    /**
     * Represents the minimum supported client version. This will be used to ensure the client and server are compatible
     * @see client.ClientConstants#VERSION
     */
    public static final double MIN_SUPPORTED_CLIENT_VERSION = 0;
    /**
     * Represents the maximum supported client version. This will be used to ensure the client and server are compatible
     * @see client.ClientConstants#VERSION
     */
    public static final double MAX_SUPPORTED_CLIENT_VERSION = 0;
    /**
     * Represents this server's version
     */
    public static final double VERSION = 0;
    /**
     * The {@link Branch} this server is in. This may activate or deactivate certain features
     */
    public static final Branch BRANCH = Branch.DEV;
    
    //GENERAL
    /**
     * The port that the server will be started on
     */
    public static final int SERVER_PORT = 5555;
    /**
     * Whether {@link java.util.concurrent.locks.ReentrantReadWriteLock}s will be fair
     */
    public static final boolean USE_FAIR_LOCKS = true;
    /**
     * The system root directory
     */
    public static final String ROOT_DIR = System.getProperty("os.name").startsWith("Windows") ? "C:\\" : "/";
    /**
     * The main server directory
     */
    public static final String SERVER_DIR = ROOT_DIR + "home" + separator + "VPT" + separator + "Server";
    /**
     * The server backup directory
     */
    public static final String BACKUP_DIR = SERVER_DIR + separator + "Backups";
    /**
     * The interval between runs of periodic methods in nanoseconds
     * @see ServerMain#startPeriodicMethods() 
     */
    public static final long PERIODIC_INTERVAL = Utils.toNanos(10, TimeUnit.MINUTES);
    
    //SQL
    /**
     * The SQL database name
     */
    public static final String SQL_DB = "VPTDB";
    /**
     * The SQL database URL
     */
    public static final String SQL_URL = "jdbc:mysql://169.254.82.12:3306/" + SQL_DB;
    /**
     * The SQL user to use to access the database
     */
    public static final String SQL_USER = "VPTAPI";
    
    //REQUEST
    /**
     * The exponent base to be used when calculating timeouts. New timeouts will be <code>TIMEOUT_BASE<sup>numberOfPreviousTimeouts+1</sup></code> seconds
     * @see server.services.RequestService#request(common.networking.ssl.SSLConnection, java.lang.String, int) 
     */
    public static final int TIMEOUT_BASE = 5;
    /**
     * The minimum amount of time (in nanoseconds) until a request can be forgotten
     * @see server.services.RequestService#shouldForgetRequest
     */
    public static final long MIN_REQUEST_FORGET_TIME = Utils.toNanos(10, TimeUnit.MINUTES);
    /**
     * The maximum amount of computer generated requests until a timeout occurs
     * @see server.services.RequestService#request(common.networking.ssl.SSLConnection, java.lang.String, int) 
     */
    public static final int AUTO_REQUESTS_TE = 500;
    /**
     * The maximum amount of standard requests until a timeout occurs
     * @see server.services.RequestService#request(common.networking.ssl.SSLConnection, java.lang.String, int) 
     */
    public static final int USER_NORM_REQUESTS_TE = 100;
    /**
     * The maximum amount of special (ex. logins) requests until a timeout occurs
     * @see server.services.RequestService#request(common.networking.ssl.SSLConnection, java.lang.String, int) 
     */
    public static final int USER_SPEC_REQUESTS_TE = 5;
    /**
     * The maximum amount of one time (ex. password change) requests until a timeout occurs
     * @see server.services.RequestService#request(common.networking.ssl.SSLConnection, java.lang.String, int) 
     */
    public static final int USER_ONET_REQUESTS_TE = 1;
    
    //USER
    /**
     * The max length for a username
     */
    public static final int USERNAME_MAX_LENGTH = 20;
    /**
     * A user readable string of characters forbidden in usernames
     */
    public static final String USERNAME_FORBIDDEN_CHARACTERS = "<>:'/\\|?*[]^$.+-(){}";
    /**
     * A pattern string of characters forbidden in usernames
     * @see java.util.regex.Pattern
     */
    public static final String USERNAME_FORBIDDEN_CHARACTERS_REGEX = "[\\Q<>:'/\\|?*[]^$.+-(){}\\E]+";
    
    private ServerConstants() {}
    
}