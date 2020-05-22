package client;

import static common.Constants.Branch;

/**
 * Constants specific to the client side of the VPT
 */
public final class ClientConstants {
    
    /**
     * The Client version. This will be used to ensure the client and server are compatible
     * @see server.ServerConstants#MIN_SUPPORTED_CLIENT_VERSION
     * @see server.ServerConstants#MAX_SUPPORTED_CLIENT_VERSION
     */
    public static final double VERSION = 0;
    
    /**
     * The {@link Branch} this client is in. This may activate or deactivate certain features
     */
    public static final Branch BRANCH = Branch.DEV;
    
    private ClientConstants() {}
    
}