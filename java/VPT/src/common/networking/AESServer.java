package common.networking;

import java.io.Closeable;
import java.io.IOException;
import java.security.cert.X509Certificate;

public class AESServer implements Closeable {

    protected final String handle;
    protected final X509Certificate cert;
    
    @SuppressWarnings("LeakingThisInConstructor")
    public AESServer(X509Certificate cert) throws IOException {
        handle = NetworkingJNI.createServer();
        this.cert = cert;
    }
    
    public AESServerConnection accept() throws IOException {
        return new AESServerConnection(NetworkingJNI.accept(handle), cert);
    }
    
    @Override
    @SuppressWarnings("ConvertToTryWithResources")
    public void close() throws IOException {
        NetworkingJNI.dispose(handle);
    }
    
}