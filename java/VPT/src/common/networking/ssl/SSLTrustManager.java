package common.networking.ssl;

import common.Utils;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import javax.net.ssl.X509TrustManager;

class SSLTrustManager implements X509TrustManager {

    protected X509Certificate[] certs;
    
    public SSLTrustManager(KeyStore keyStore) throws KeyStoreException {
        Enumeration<String> aliases = keyStore.aliases();
        int foundCerts = 0;
        X509Certificate[] temp = new X509Certificate[keyStore.size()];
        while(aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if(keyStore.isCertificateEntry(alias)) {
                Certificate cert = keyStore.getCertificate(alias);
                if(cert instanceof X509Certificate) {
                    temp[foundCerts] = (X509Certificate)cert;
                    foundCerts++;
                }
            }
        }
        certs = new X509Certificate[foundCerts];
        System.arraycopy(temp, 0, certs, 0, foundCerts);
    }
    
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if(chain == null || chain.length == 0) {
            throw new IllegalArgumentException("Chain is empty");
        }
        if(authType == null || authType.isEmpty()) {
            throw new IllegalArgumentException("Authtype is empty");
        }
        for(X509Certificate cert: certs) {
            if(Utils.contains(chain, cert)) {
                return;
            }
        }
        throw new CertificateException("Certificate Chain Untrusted");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        checkClientTrusted(chain, authType);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return Arrays.copyOf(certs, certs.length);
    }
    
}