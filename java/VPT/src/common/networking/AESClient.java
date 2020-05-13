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
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class AESClient extends JNINetworkInterface {

    private final CipherInputStream cis;
    private final CipherOutputStream cos;
    private Cipher eCipher, dCipher;
    private X509Certificate serverCert;

    @SuppressWarnings("LeakingThisInConstructor")
    public AESClient() throws IOException {
        super(NetworkingJNI.createClient());
        try(ObjectInputStream tois = new ObjectInputStream(is)) {
            serverCert = (X509Certificate)tois.readObject();
            //AUTHENTICATE CERT
            eCipher = Cipher.getInstance(Constants.ENCRYPTION_MODE);
            dCipher = Cipher.getInstance(Constants.ENCRYPTION_MODE);
            SecretKey key = KeyGenerator.getInstance("AES").generateKey();
            eCipher.init(Cipher.ENCRYPT_MODE, key);
            dCipher.init(Cipher.DECRYPT_MODE, key);
            byte[] eIV = eCipher.getIV();
            byte[] dIV = dCipher.getIV();
            Cipher tCipher = Cipher.getInstance(serverCert.getPublicKey().getAlgorithm() + "/CBC/PKCS5Padding");
            tCipher.init(Cipher.ENCRYPT_MODE, serverCert);
            try(ObjectOutputStream toos = new ObjectOutputStream(new CipherOutputStream(os, tCipher))) {
                toos.writeObject(key);
                toos.writeObject(eIV);
                toos.writeObject(dIV);
            }
        } catch(Exception e) {
            throw new IOException("Error in handshake", e);
        }
        os.write(0);
        os.flush();
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