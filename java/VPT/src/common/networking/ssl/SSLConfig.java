package common.networking.ssl;

import common.Constants;
import common.Utils;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * A class with special methods for interacting with a {@link SSLContext}
 */
public final class SSLConfig {
    
    /**
     * The {@link SSLContext} which methods interact with
     */
    private static final SSLContext context = Utils.catchNoSuchAlgorithmException(() -> SSLContext.getInstance(Constants.SSL_MODE));
    /**
     * The {@link SSLSocketFactory} used in creating {@link SSLSocket}s
     * @see #createSocket(java.lang.String, int) 
     */
    private static SSLSocketFactory socketFactory;
    /**
     * The {@link SSLServerSocketFactory} used in creating {@link SSLServerSocket}s
     * @see #createServerSocket(int) 
     */
    private static SSLServerSocketFactory serverSocketFactory;
    /**
     * Whether the {@link SSLContext} has been initialized
     */
    private static boolean init = false;
    
    /**
     * Initializes the {@link SSLContext} for client use
     * @param trustStore the {@link KeyStore} to verify trusted certificates
     * @throws KeyManagementException if the SSLContext cannot be initialized
     * @throws KeyStoreException if there is an error reading the trustStore
     * @see SSLConfig#initServer(java.security.KeyStore, char[], java.lang.String) 
     * @see SSLConfig#initBoth(java.security.KeyStore, char[], java.lang.String, java.security.KeyStore) 
     * @see SSLContext#init(javax.net.ssl.KeyManager[], javax.net.ssl.TrustManager[], java.security.SecureRandom) 
     */
    public static void initClient(KeyStore trustStore) throws KeyManagementException, KeyStoreException {
        context.init(null, new TrustManager[] {new SSLTrustManager(trustStore)}, null);
        init();
    }
    
    /**
     * Initializes the {@link SSLContext} for server use
     * @param keyStore a {@link KeyStore} containing the key to use when creating SSL connections
     * @param password the password for the key
     * @param keyAlias the alias of the key
     * @throws KeyManagementException if the SSLContext cannot be initialized
     * @see SSLConfig#initClient(java.security.KeyStore) 
     * @see SSLConfig#initBoth(java.security.KeyStore, char[], java.lang.String, java.security.KeyStore) 
     * @see SSLContext#init(javax.net.ssl.KeyManager[], javax.net.ssl.TrustManager[], java.security.SecureRandom) 
     */
    public static void initServer(KeyStore keyStore, char[] password, String keyAlias) throws KeyManagementException {
        context.init(new KeyManager[] {new SSLKeyManager(keyStore, password, keyAlias)}, null, null);
        init();
    }
    
    /**
     * Initializes the {@link SSLContext} for both client and server use
     * @param keyStore a {@link KeyStore} containing the key to use when creating SSL connections
     * @param password the password for the key
     * @param keyAlias the alias of the key
     * @param trustStore the {@link KeyStore} to verify trusted certificates
     * @throws KeyManagementException if the SSLContext cannot be initialized
     * @throws KeyStoreException if there is an error reading the trustStore
     * @see SSLConfig#initClient(java.security.KeyStore) 
     * @see SSLConfig#initServer(java.security.KeyStore, char[], java.lang.String) 
     * @see SSLContext#init(javax.net.ssl.KeyManager[], javax.net.ssl.TrustManager[], java.security.SecureRandom) 
     */
    public static void initBoth(KeyStore keyStore, char[] password, String keyAlias, KeyStore trustStore) throws KeyManagementException, KeyStoreException {
        context.init(new KeyManager[] {new SSLKeyManager(keyStore, password, keyAlias)}, new TrustManager[] {new SSLTrustManager(trustStore)}, null);
        init();
    }
    
    /**
     * Initializes the socket factories
     */
    private static void init() {
        socketFactory = context.getSocketFactory();
        serverSocketFactory = context.getServerSocketFactory();
        init = true;
    }
    
    /**
     * Creates a {@link SSLSocket}
     * @param host the host to connect to
     * @param port the port to connect to
     * @return a SSLSocket connected to the specified location
     * @throws IllegalStateException if the SSLContext is not initialized
     * @throws IOException if there is an error creating the socket
     * @see SSLSocketFactory#createSocket(java.lang.String, int) 
     */
    public static SSLSocket createSocket(String host, int port) throws IllegalStateException, IOException {
        checkInit();
        return (SSLSocket)socketFactory.createSocket(host, port);
    }
    
    /**
     * Creates a {@link SSLServerSocket}
     * @param port the port to host the server on
     * @return a SSLServerSocket connected to the specified location
     * @throws IllegalStateException if the SSLContext is not initialized
     * @throws IOException if there is an error creating the server
     * @see SSLServerSocketFactory#createServerSocket(int) 
     */
    public static SSLServerSocket createServerSocket(int port) throws IllegalStateException, IOException {
        checkInit();
        return (SSLServerSocket)serverSocketFactory.createServerSocket(port);
    }
    
    /**
     * Checks if the {@link SSLContext} is initialized
     * @throws IllegalStateException if the SSLContext is not initialized
     */
    private static void checkInit() throws IllegalStateException {
        if(!init) {
            throw new IllegalStateException("Not Initialized");
        }
    }
    
    private SSLConfig() {}
    
}