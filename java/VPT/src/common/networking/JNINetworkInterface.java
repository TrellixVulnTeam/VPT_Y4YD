package common.networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class JNINetworkInterface implements AutoCloseable {
    
    protected final String handle;
    protected final InputStream is;
    protected final OutputStream os;
    protected boolean isConnected, isClosed;

    protected JNINetworkInterface(String handle) {
        isConnected = false;
        isClosed = false;
        this.handle = handle;
        is = new InputStream() {
            @Override
            public int read() throws IOException {
                return NetworkingJNI.recieveData(handle);
            }
        };
        os = new OutputStream() {
            ArrayList<Byte> buf = new ArrayList<>();
            @Override
            public void write(int arg0) throws IOException {
                buf.add((byte)arg0);
                //1 megabyte
                if(buf.size() >= 1024 * 1024) {
                    flush();
                }
            }

            @Override
            public void flush() throws IOException {
                if(buf.isEmpty()) return;
                Byte[] out = new Byte[buf.size()];
                buf.toArray(out);
                byte[] pOut = new byte[buf.size()];
                for(int i = 0; i < out.length; i++) {
                    pOut[i] = out[i];
                }
                NetworkingJNI.sendData(handle, pOut, out.length);
            }
        };
    }
    
    public InputStream getInputStream() {
        return is;
    }
    
    public OutputStream getOutputStream() {
        return os;
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    public boolean isClosed() {
        return isClosed;
    }
    
    @Override
    public void close() throws IOException {
        if(!isClosed) {
            isConnected = false;
            isClosed = true;
            NetworkingJNI.dispose(handle);
        }
    }
    
}