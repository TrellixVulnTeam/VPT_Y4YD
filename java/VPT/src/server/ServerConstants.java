package server;

import static common.Constants.Branch;
import java.io.File;
import static java.io.File.separator;

public final class ServerConstants {
    
    public static final double MIN_SUPPORTED_CLIENT_VERSION = 0;
    public static final double MAX_SUPPORTED_CLIENT_VERSION = 0;
    public static final double VERSION = 0;
    public static final Branch BRANCH = Branch.DEV;
    public static final boolean USE_FAIR_LOCKS = true;
    public static final String SERVER_DIR = new File("/").getAbsolutePath() + "VPT" + separator + "Server";
    public static final String BACKUP_DIR = SERVER_DIR + separator + "Backups";
    
    private ServerConstants() {}
    
}