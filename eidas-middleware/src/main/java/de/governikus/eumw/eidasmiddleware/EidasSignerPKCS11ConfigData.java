package de.governikus.eumw.eidasmiddleware;

import se.elegnamnden.eidas.pkcs11.*;
import se.elegnamnden.eidas.pkcs11.impl.GenericPKCS11Provider;
import se.elegnamnden.eidas.pkcs11.impl.PKCS11ExternalCfgProvider;
import se.elegnamnden.eidas.pkcs11.impl.PKCS11NullProvider;
import se.elegnamnden.eidas.pkcs11.impl.PKCS11SoftHsmProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

public class EidasSignerPKCS11ConfigData {

    private static final Logger log = Logger.getLogger(EidasSignerPKCS11ConfigData.class.getName());
    private String[] hsmExternalCfgLocations;
    private String hsmKeyLocation;
    private String hsmPin;
    private String hsmLib;
    private String hsmProviderName;
    private String hsmSlot;
    int hsmSlotListIndex;
    int hsmSlotListIndexMaxRange;
    private String keySourcePass, keySourceAlias, keySourceCertLocation;
    private String keySourcePassMd, keySourceAliasMd, keySourceCertLocationMd;

    public EidasSignerPKCS11ConfigData(String pkcs11ConfigLocation) throws IOException {
        log.info("Attempting to load PKCS#11 configuration from: " + pkcs11ConfigLocation);
        Properties prop = new Properties();
        prop.load(new FileInputStream(new File(pkcs11ConfigLocation)));
        hsmExternalCfgLocations = getPropertyArray(prop.getProperty("hsmExternalCfgLocations"),",");
        hsmKeyLocation = prop.getProperty("hsmKeyLocation");
        hsmPin = prop.getProperty("hsmPin");
        hsmLib = prop.getProperty("hsmLib");
        hsmProviderName = prop.getProperty("hsmProviderName");
        hsmSlot = prop.getProperty("hsmSlot");
        hsmSlotListIndex = getInt(prop.getProperty("hsmSlotListIndex"));
        hsmSlotListIndexMaxRange = getInt(prop.getProperty("hsmSlotListIndexMaxRange"));
        keySourcePass = prop.getProperty("keySourcePass");
        keySourceAlias = prop.getProperty("keySourceAlias");
        keySourceCertLocation = prop.getProperty("keySourceCertLocation");
        keySourcePassMd = prop.getProperty("keySourcePassMd");
        keySourceAliasMd = prop.getProperty("keySourceAliasMd");
        keySourceCertLocationMd = prop.getProperty("keySourceCertLocationMd");
        log.info("PKCS#11 configuration loaded");
    }

    private String[] getPropertyArray(String data, String split) {
        if (data==null){
            return null;
        }
        return data.split(split);
    }

    private int getInt(String intStr) {
        try {
            return Integer.valueOf(intStr);
        } catch (Exception ex) {
            return 0;
        }
    }

    public PKCS11Provider getPKCS11Provider() throws Exception {
        PKCS11ProviderConfiguration configuration;
        if (hsmExternalCfgLocations != null) {
            configuration = new PKCS11ProvidedCfgConfiguration(Arrays.asList(hsmExternalCfgLocations));
            log.info("Setting up PKCS11 configuration based on externally provided PKCS11 config files");
        } else {
            if (hsmKeyLocation != null && hsmPin != null) {
                PKCS11SoftHsmProviderConfiguration softHsmConfig = new PKCS11SoftHsmProviderConfiguration();
                softHsmConfig.setKeyLocation(hsmKeyLocation);
                softHsmConfig.setPin(hsmPin);
                configuration = softHsmConfig;
                log.info("Setting up PKCS11 configuration based on SoftHSM");
            } else {
                configuration = new PKCS11ProviderConfiguration();
                log.info("Setting up generic PKCS11 configuration");
            }
            configuration.setLibrary(hsmLib);
            configuration.setName(hsmProviderName);
            configuration.setSlot(hsmSlot);
            configuration.setSlotListIndex(hsmSlotListIndex);
            configuration.setSlotListIndexMaxRange(hsmSlotListIndexMaxRange);
        }

        PKCS11Provider pkcs11Provider = createInstance(configuration);
        return pkcs11Provider;
    }

    protected PKCS11Provider createInstance(PKCS11ProviderConfiguration configuration) throws Exception {
        if (PKCS11ProvidedCfgConfiguration.class.isInstance(configuration)) {
            PKCS11ProvidedCfgConfiguration providedCfgConfig = PKCS11ProvidedCfgConfiguration.class.cast(configuration);
            if (providedCfgConfig.getConfigLocationList() != null && !providedCfgConfig.getConfigLocationList().isEmpty()) {
                log.info("Found PKCS11 configuration for externally provided cfg files for PKCS11 token/HSM");
                return new PKCS11ExternalCfgProvider(providedCfgConfig);
            }
        }

        if (configuration.getLibrary() != null && configuration.getName() != null) {
            if (PKCS11SoftHsmProviderConfiguration.class.isInstance(configuration)) {
                PKCS11SoftHsmProviderConfiguration softHsmConfig = PKCS11SoftHsmProviderConfiguration.class.cast(configuration);
                if (softHsmConfig.getKeyLocation() != null && softHsmConfig.getPin() != null) {
                    log.info("Found PKCS11 configuration for SoftHSM");
                    return new PKCS11SoftHsmProvider(softHsmConfig);
                }
            }

            log.info("Found PKCS11 configuration for PKCS11 token/HSM");
            return new GenericPKCS11Provider(configuration);
        } else {
            this.log.info("No valid PKCS11 configuration found");
            return new PKCS11NullProvider();
        }
    }

    public String getKeySourcePass() {
        return keySourcePass;
    }

    public String getKeySourceAlias() {
        return keySourceAlias;
    }

    public String getKeySourceCertLocation() {
        return keySourceCertLocation;
    }

    public String getKeySourcePassMd() {
        return keySourcePassMd;
    }

    public String getKeySourceAliasMd() {
        return keySourceAliasMd;
    }

    public String getKeySourceCertLocationMd() {
        return keySourceCertLocationMd;
    }
}
