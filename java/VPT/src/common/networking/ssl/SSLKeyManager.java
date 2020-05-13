package common.networking.ssl;

import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509KeyManager;

class SSLKeyManager implements X509KeyManager {

    protected KeyStore keyStore;
    protected char[] password;
    protected String alias;
    private String type;
    private String issuer;

    public SSLKeyManager(KeyStore keyStore, char[] password, String alias) {
        this.keyStore = keyStore;
        this.password = password;
        this.alias = alias;
        try {
            Certificate cert = keyStore.getCertificate(alias);
            type = cert.getPublicKey().getAlgorithm();
            issuer = ((X509Certificate)cert).getIssuerDN().getName();
        } catch(ClassCastException | KeyStoreException e) {
            throw new IllegalArgumentException("Bad Alias: " + alias, e);
        }
    }

    @SuppressWarnings("LocalVariableHidesMemberVariable")
    public String chooseClientAlias(String[] types, Principal[] issuers) {
        if(types == null) {
            return alias;
        }
        for(String type: types) {
            if(!type.equals(this.type)) {
                continue;
            }
            if(issuers == null) {
                return alias;
            }
            for(Principal issuer: issuers) {
                if(this.issuer.equals(issuer.getName())) {
                    return alias;
                }
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    public String[] getClientAliases(String type, Principal[] issuers) {
        String alias = chooseClientAlias(new String[] {type}, issuers);
        if(alias == null) {
            return new String[0];
        } else {
            String[] out = new String[1];
            out[0] = alias;
            return out;
        }
    }

    @Override
    public String chooseClientAlias(String[] type, Principal[] issuers, Socket socket) {
        return chooseClientAlias(type, issuers);
    }

    @Override
    public String[] getServerAliases(String type, Principal[] issuers) {
        return getClientAliases(type, issuers);
    }

    @Override
    public String chooseServerAlias(String type, Principal[] issuers, Socket socket) {
        return chooseClientAlias(new String[] {type}, issuers, socket);
    }

    @Override
    public X509Certificate[] getCertificateChain(String string) {
        try {
            Certificate[] certs = keyStore.getCertificateChain(alias);
            int validCerts = 0;
            X509Certificate[] temp = new X509Certificate[certs.length];
            for(int i = 0; i < certs.length; i++) {
                Certificate cert = certs[i];
                if(cert instanceof X509Certificate) {
                    temp[i] = (X509Certificate)cert;
                    validCerts++;
                }
            }
            if(validCerts == certs.length) {
                return temp;
            }
            X509Certificate[] out = new X509Certificate[validCerts];
            int j = 0;
            for(X509Certificate cert: temp) {
                if(cert != null) {
                    out[j] = cert;
                    j++;
                }
            }
            return out;
        } catch(KeyStoreException e) {
            return null;
        }
    }

    @Override
    public PrivateKey getPrivateKey(String alias) {
        try {
            return (PrivateKey)keyStore.getKey(alias, password);
        } catch(ClassCastException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            return null;
        }
    }
    
}