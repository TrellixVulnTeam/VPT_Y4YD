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

public final class SSLConfig {
    
    private static final SSLContext context = Utils.catchNoSuchAlgorithmException(() -> SSLContext.getInstance(Constants.TLS_MODE));
    private static SSLSocketFactory socketFactory;
    private static SSLServerSocketFactory serverSocketFactory;
    private static boolean init = false;
    
    public static void initClient(KeyStore keyStore) throws KeyManagementException, KeyStoreException {
        context.init(null, new TrustManager[] {new SSLTrustManager(keyStore)}, null);
        init();
    }
    
    public static void initServer(KeyStore keyStore, char[] password, String keyAlias) throws KeyManagementException {
        context.init(new KeyManager[] {new SSLKeyManager(keyStore, password, keyAlias)}, null, null);
        init();
    }
    
    private static void init() {
        socketFactory = context.getSocketFactory();
        serverSocketFactory = context.getServerSocketFactory();
        init = true;
    }
    
    public static SSLSocket createSocket(String host, int port) throws IllegalStateException, IOException {
        checkInit();
        return (SSLSocket)socketFactory.createSocket(host, port);
    }
    
    public static SSLServerSocket createServerSocket(int port) throws IllegalStateException, IOException {
        checkInit();
        return (SSLServerSocket)serverSocketFactory.createServerSocket(port);
    }
    
    private static void checkInit() throws IllegalStateException {
        if(!init) {
            throw new IllegalStateException("Not Initialized");
        }
    }
    
    private SSLConfig() {}
    
}