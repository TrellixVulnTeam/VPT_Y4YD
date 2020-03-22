package common.networking;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class AESSocket implements Closeable {

    private final JNINetworkInterface _interface;
    private final ObjectInputStream is;
    private final ObjectOutputStream os;
    
    public AESSocket(JNINetworkInterface _interface) throws IOException {
        this._interface = _interface;
        is = new ObjectInputStream(_interface.getInputStream());
        os = new ObjectOutputStream(_interface.getOutputStream());
    }

    public JNINetworkInterface getInterface() {
        return _interface;
    }
    
    public ObjectInputStream getInputStream() {
        return is;
    }
    
    public ObjectOutputStream getOutputStream() {
        return os;
    }
    
    @Override
    public void close() throws IOException {
        is.close();
        os.close();
        _interface.close();
    }
    
}