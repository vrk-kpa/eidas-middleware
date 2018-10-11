/*
 * The eidas-opensaml project is an open-source package that extends OpenSAML
 * with definitions for the eIDAS Framework.
 *
 * More details on <https://github.com/litsec/eidas-opensaml>
 * Copyright (C) 2016 Litsec AB
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.governikus.eumw.eidasstarterkit.ossigner;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * Abstract base class that initializes OpenSAML for test classes.
 *
 * @author Martin Lindström (martin.lindstrom@litsec.se)
 */
public abstract class OpenSAMLBase {

    /**
     * Logger instance.
     */
    private static Logger logger = LoggerFactory.getLogger(OpenSAMLBase.class);

    /**
     * Utility method for creating an OpenSAML object given its element name.
     *
     * @param clazz the class to create
     * @param elementName the element name for the XML object to create
     * @return the XML object
     */
    public static <T extends XMLObject> T createSamlObject(Class<T> clazz, QName elementName) {
        if (!XMLObject.class.isAssignableFrom(clazz)) {
            throw new RuntimeException(String.format("%s is not a XMLObject class", clazz.getName()));
        }
        XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
        XMLObjectBuilder<? extends XMLObject> builder = builderFactory.getBuilder(elementName);
        if (builder == null) {
            // No builder registered for the given element name. Try creating a builder for the default element name.
            builder = builderFactory.getBuilder(getDefaultElementName(clazz));
        }
        Object object = builder.buildObject(elementName);
        return clazz.cast(object);
    }

    /**
     * Returns the default element name for the supplied class
     *
     * @param clazz class to check
     * @return the default QName
     */
    public static <T extends XMLObject> QName getDefaultElementName(Class<T> clazz) {
        try {
            return (QName) clazz.getDeclaredField("DEFAULT_ELEMENT_NAME").get(null);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the builder object that can be used to build object for the given
     * element name.
     *
     * @param elementName the element name for the XML object that the builder
     * should return
     * @return a builder object
     */
    @SuppressWarnings("unchecked")
    public static <T extends XMLObject> XMLObjectBuilder<T> getBuilder(QName elementName) {
        return (XMLObjectBuilder<T>) XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(elementName);
    }

    /**
     * Marshalls the supplied {@code XMLObject} into an {@code Element}.
     *
     * @param object the object to marshall
     * @return an XML element
     * @throws MarshallingException for marshalling errors
     */
    public static <T extends XMLObject> Element marshall(T object) throws MarshallingException {
        return XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(object).marshall(object);
    }

    /**
     * Unmarshalls the supplied element into the given type.
     *
     * @param xml the DOM (XML) to unmarshall
     * @param targetClass the required class
     * @return an {@code XMLObject} of the given type
     * @throws UnmarshallingException for unmarshalling errors
     */
    public static <T extends XMLObject> T unmarshall(Element xml, Class<T> targetClass) throws UnmarshallingException {
        XMLObject xmlObject = XMLObjectProviderRegistrySupport.getUnmarshallerFactory().getUnmarshaller(xml).unmarshall(xml);
        return targetClass.cast(xmlObject);
    }

    public static Credential getCredentialsFromKeyStore(KeyStore keyStore, char[] ksPass, String alias) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        if (keyStore == null) {
            return null;
        }
        BasicCredential credential = new BasicCredential(keyStore.getCertificate(alias).getPublicKey(), (PrivateKey) keyStore.getKey(alias, ksPass));
        credential.setUsageType(UsageType.UNSPECIFIED);

        return credential;
    }

    public static Credential getCredentialFromCert(byte[] cert, String entityId) throws CertificateException, IOException {
        return getCredentialFromCert(SignSamlUtil.getCert(cert).getPublicKey(), entityId);        
    }
    
    public static Credential getCredentialFromCert(PublicKey pubKey, String entityID) {
        BasicCredential credential = new BasicCredential(pubKey);
        credential.setUsageType(UsageType.SIGNING);
        credential.setEntityId(entityID);
        return credential;
    }
}
