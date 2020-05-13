package server;

import common.Constants.Branch;
import common.Utils;
import java.io.File;
import static java.io.File.separator;
import java.util.concurrent.TimeUnit;

public final class ServerConstants {
    
    //VERSION
    public static final double MIN_SUPPORTED_CLIENT_VERSION = 0;
    public static final double MAX_SUPPORTED_CLIENT_VERSION = 0;
    public static final double VERSION = 0;
    public static final Branch BRANCH = Branch.DEV;
    
    //GENERAL
    public static final boolean USE_FAIR_LOCKS = true;
    public static final String SERVER_DIR = new File("/").getAbsolutePath() + "VPT" + separator + "Server";
    public static final String BACKUP_DIR = SERVER_DIR + separator + "Backups";
    public static final long PERIODIC_INTERVAL = Utils.toNanos(10, TimeUnit.MINUTES);
    
    //REQUEST
    public static final long MIN_REQUEST_FORGET_TIME = Utils.toNanos(10, TimeUnit.MINUTES);
    public static final int USER_REQUESTS_TE = 5;
    public static final int USER_SPEC_REQUESTS_TE = 1;
    
    //USER
    public static final String USERID_FORBIDDEN_CHARACTERS = "<>:'/\\|?*[]^$.+-(){}";
    public static final String USERID_FORBIDDEN_CHARACTERS_REGEX = "[\\Q<>:'/\\|?*[]^$.+-(){}\\E]+";
    
    private ServerConstants() {}
    
}