/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.governikus.eumw.eidasstarterkit.ossigner;

import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.X509Certificate;
import org.opensaml.xmlsec.signature.X509Data;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.Signer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.List;

/**
 *
 * @author stefan
 */
public class SignSamlUtil {

    private final static Logger logger = LoggerFactory.getLogger(SignSamlUtil.class);

    private SignSamlUtil() {
    }

    public static void sign(SignableSAMLObject tbsObject, KeyStore keyStore, char[] kspass, String alias)
            throws SecurityException, SignatureException, MarshallingException, org.opensaml.xmlsec.signature.support.SignatureException,
            KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException, IOException {
        BasicX509Credential signingCredential = getCredential(keyStore, kspass, alias);
        sign(tbsObject, signingCredential, signingCredential.getEntityCertificate());
    }

    public static void sign(SignableSAMLObject tbsObject, Credential signingCredential, java.security.cert.X509Certificate certificate)
            throws SecurityException, SignatureException, MarshallingException, CertificateEncodingException,
            org.opensaml.xmlsec.signature.support.SignatureException {
        Signature signature = getSignatureElement(signingCredential, certificate);
        tbsObject.setSignature(signature);
        XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(tbsObject).marshall(tbsObject);
        Signer.signObject(signature);
    }

    private static Signature getSignatureElement(Credential signingCredential, java.security.cert.X509Certificate certificate) throws CertificateEncodingException {
        Signature signature = OpenSAMLBase.createSamlObject(Signature.class, Signature.DEFAULT_ELEMENT_NAME);
        signature.setSigningCredential(signingCredential);
        signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        signature.setKeyInfo(getKeyInfo(certificate));
        return signature;
    }

    private static KeyInfo getKeyInfo(java.security.cert.X509Certificate x509cert) throws CertificateEncodingException {
        KeyInfo keyInfo = OpenSAMLBase.createSamlObject(KeyInfo.class, KeyInfo.DEFAULT_ELEMENT_NAME);
        X509Data x509Data = OpenSAMLBase.createSamlObject(X509Data.class, X509Data.DEFAULT_ELEMENT_NAME);
        X509Certificate cert = OpenSAMLBase.createSamlObject(X509Certificate.class, X509Certificate.DEFAULT_ELEMENT_NAME);
        List<X509Data> x509DataList = keyInfo.getX509Datas();
        x509DataList.add(x509Data);
        List<X509Certificate> x509Certificates = x509Data.getX509Certificates();
        x509Certificates.add(cert);
        cert.setValue(String.valueOf(Base64Coder.encode(x509cert.getEncoded())));
        return keyInfo;
    }

    private static BasicX509Credential getCredential(KeyStore keyStore, char[] kspass, String alias)
            throws KeyStoreException, CertificateEncodingException, NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException, IOException {
        BasicX509Credential credential = new BasicX509Credential(getCert(keyStore.getCertificate(alias).getEncoded()), (PrivateKey) keyStore.getKey(alias, kspass));
        return credential;
    }

    public static java.security.cert.X509Certificate getCert(byte[] certBytes) throws CertificateException, IOException {
        try (InputStream inStream = new ByteArrayInputStream(certBytes)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            java.security.cert.X509Certificate cert = (java.security.cert.X509Certificate) cf.generateCertificate(inStream);
            return cert;
        }
    }

/*    public static OsSigvalResult validateSignature(SignableSAMLObject signedObject, List<java.security.cert.X509Certificate> sigValCerts) {
        try {
            if (signedObject.getSignature() == null) {
                // Signable object is not signed
                return new OsSigvalResult();
            }
        } catch (Exception ex) {
            // This is not a signable object;
            return new OsSigvalResult();
        }

        // Verify signature
        try {
            // This prevalidation is done in order to set the ID attributes as ID.
            // This is necessary for the validaiton process against the credentials.
            Element sigObjelm = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(signedObject).marshall(signedObject);
            byte[] sigObjBytes = SerializeSupport.nodeToString(sigObjelm).getBytes(StandardCharsets.UTF_8);
            SigVerifyResult verifySignature = XMLSign.verifySignature(sigObjBytes);

            //OpenSAML validation (Validates against signer certificate)
            //SignatureValidator.validate(signedObject.getSignature(), idpCredential);
            if (verifySignature.valid) {
                for (java.security.cert.X509Certificate cert : sigValCerts) {
                    if (verifySignature.cert.getPublicKey().equals(cert.getPublicKey())) {
                        return new OsSigvalResult(true, verifySignature.cert);
                    }
                }
                // valid signature
            }
            return new OsSigvalResult(false);
        } catch (Exception ex) {
            return new OsSigvalResult(ex);
        }
    }*/
}
