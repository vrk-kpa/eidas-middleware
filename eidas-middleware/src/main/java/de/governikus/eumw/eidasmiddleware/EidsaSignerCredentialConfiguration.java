package de.governikus.eumw.eidasmiddleware;

import de.governikus.eumw.eidasstarterkit.XMLSignatureHandler;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.opensaml.security.x509.BasicX509Credential;
import se.elegnamnden.eidas.pkcs11.PKCS11Provider;
import se.elegnamnden.eidas.pkcs11.impl.PKCS11NoTestCredential;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

public class EidsaSignerCredentialConfiguration {

    private static final Logger log = Logger.getLogger(EidsaSignerCredentialConfiguration.class.getName());
    private static BasicX509Credential samlMessageSigningCredential;
    private static BasicX509Credential metadataSigningCredential;
    private static String pkcs11ConfigLocation;
    private static EidasSignerPKCS11ConfigData pkcs11Config;

    static {
        pkcs11ConfigLocation = System.getenv("PKCS11_CONFIG_LOCATION");
        if (pkcs11ConfigLocation == null) {
            pkcs11Config = null;
            loadDefaultKeys();
            log.info("Loaded default file based signing keys for SAML and Metadata signing");
        } else {
            try {
                pkcs11Config = new EidasSignerPKCS11ConfigData(pkcs11ConfigLocation);
                PKCS11Provider pkcs11Provider = pkcs11Config.getPKCS11Provider();
                samlMessageSigningCredential = new PKCS11NoTestCredential(
                        getCert(pkcs11Config.getKeySourceCertLocation()),
                        pkcs11Provider.getProviderNameList(),
                        pkcs11Config.getKeySourceAlias(),
                        pkcs11Config.getKeySourcePass()
                );
                log.info("Loaded SAML signing key from PKCS#11 provider source");
                metadataSigningCredential = new PKCS11NoTestCredential(
                        getCert(pkcs11Config.getKeySourceCertLocationMd()),
                        pkcs11Provider.getProviderNameList(),
                        pkcs11Config.getKeySourceAliasMd(),
                        pkcs11Config.getKeySourcePassMd()
                );
                log.info("Loaded Metadata signing key from PKCS#11 provider source");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static X509Certificate getCert(String keySourceCertLocation) throws IOException, CertificateException {
        InputStream inStream = null;
        try {
            inStream = new FileInputStream(keySourceCertLocation);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
            return cert;
        } finally {
            if (inStream != null) {
                inStream.close();
            }
        }
    }

    private EidsaSignerCredentialConfiguration() {
    }

    private static void loadDefaultKeys() {
        try {
            samlMessageSigningCredential = XMLSignatureHandler.getCredential(ConfigHolder.getAppSignatureKeyPair().getKey(),
                    ConfigHolder.getAppSignatureKeyPair().getCert());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            metadataSigningCredential = XMLSignatureHandler.getCredential(ConfigHolder.getSignatureKey(),
                    ConfigHolder.getSignatureCert());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BasicX509Credential getSamlMessageSigningCredential() {
        return samlMessageSigningCredential;
    }

    public static BasicX509Credential getMetadataSigningCredential() {
        return metadataSigningCredential;
    }
}
