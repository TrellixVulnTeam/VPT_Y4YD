package common.networking;

import common.Constants;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.cert.X509Certificate;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class AESServerConnection extends JNINetworkInterface {
    private final CipherInputStream cis;
    private final CipherOutputStream cos;
    private Cipher eCipher, dCipher;

    @SuppressWarnings("LeakingThisInConstructor")
    public AESServerConnection(String handle, X509Certificate cert) throws IOException {
        super(handle);
        try {
            Cipher tCipher = Cipher.getInstance(cert.getPublicKey().getAlgorithm() + "/CBC/PKCS5Padding");
            tCipher.init(Cipher.DECRYPT_MODE, cert);
            try(ObjectInputStream tois = new ObjectInputStream(new CipherInputStream(is, tCipher)); ObjectOutputStream toos = new ObjectOutputStream(os)) {
                toos.writeObject(cert);
                eCipher = Cipher.getInstance(Constants.ENCRYPTION_MODE);
                dCipher = Cipher.getInstance(Constants.ENCRYPTION_MODE);
                SecretKey key = (SecretKey)tois.readObject();
                dCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec((byte[])tois.readObject()));
                eCipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec((byte[])tois.readObject()));
            }
        } catch (Exception e) {
            throw new IOException("Error in handshake", e);
        }
        is.read();
        cis = new CipherInputStream(is, dCipher);
        cos = new CipherOutputStream(os, eCipher);
        isConnected = true;
    }
    
    @Override
    public InputStream getInputStream() {
        return cis;
    }
    
    @Override
    public OutputStream getOutputStream() {
        return cos;
    }
    
    @Override
    @SuppressWarnings("ConvertToTryWithResources")
    public void close() throws IOException {
        cis.close();
        cos.close();
        super.close();
    }
}