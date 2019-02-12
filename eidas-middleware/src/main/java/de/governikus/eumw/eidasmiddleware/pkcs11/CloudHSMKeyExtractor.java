package de.governikus.eumw.eidasmiddleware.pkcs11;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.cavium.provider.CaviumProvider;
import se.swedenconnect.opensaml.pkcs11.credential.CustomKeyExtractor;

/**
 *
 * @author Sami Karvonen
 */
public class CloudHSMKeyExtractor implements CustomKeyExtractor{
    private static final Logger log = Logger.getLogger(CloudHSMKeyExtractor.class.getName());
    
    @Override
    public PrivateKey getPrivateKey(String providerName, String alias) {
        PrivateKey privatekey = null;
        try {        
            Security.addProvider(new CaviumProvider());
            KeyStore keyStore = KeyStore.getInstance("Cavium");
            keyStore.load(null, null);
            privatekey = (PrivateKey) keyStore.getKey(alias, null);
            
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException ex) {
            log.log(Level.SEVERE, null, ex);
        }
        return privatekey;
    }
    
}
