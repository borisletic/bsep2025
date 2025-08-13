package rs.ac.uns.ftn.pkisystem.service;

import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.pkisystem.exception.KeystoreException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.UUID;

@Service
public class KeystoreService {

    private static final String KEYSTORE_PATH = "keystores/pki-keystore.p12";
    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String MASTER_KEY = "MyMasterKeyForEncryption"; // In production, use proper key management

    public String storeKeyPair(String alias, KeyPair keyPair, X509Certificate certificate) {
        try {
            // Generate random password for this keystore entry
            String password = generateRandomPassword();

            // Load or create keystore
            KeyStore keystore = loadOrCreateKeystore();

            // Store the private key and certificate
            Certificate[] chain = {certificate};
            keystore.setKeyEntry(alias, keyPair.getPrivate(), password.toCharArray(), chain);

            // Save keystore
            saveKeystore(keystore);

            // Return encrypted password
            return encryptPassword(password);

        } catch (Exception e) {
            throw new KeystoreException("Failed to store key pair: " + e.getMessage(), e);
        }
    }

    public void storeCertificate(String alias, X509Certificate certificate) {
        try {
            // Load or create keystore
            KeyStore keystore = loadOrCreateKeystore();

            // Store the certificate
            keystore.setCertificateEntry(alias, certificate);

            // Save keystore
            saveKeystore(keystore);

        } catch (Exception e) {
            throw new KeystoreException("Failed to store certificate: " + e.getMessage(), e);
        }
    }

    public KeyPair loadKeyPair(String alias, String encryptedPassword) {
        try {
            String password = decryptPassword(encryptedPassword);
            KeyStore keystore = loadOrCreateKeystore();

            PrivateKey privateKey = (PrivateKey) keystore.getKey(alias, password.toCharArray());
            Certificate certificate = keystore.getCertificate(alias);
            PublicKey publicKey = certificate.getPublicKey();

            return new KeyPair(publicKey, privateKey);

        } catch (Exception e) {
            throw new KeystoreException("Failed to load key pair: " + e.getMessage(), e);
        }
    }

    public X509Certificate loadCertificate(String alias) {
        try {
            KeyStore keystore = loadOrCreateKeystore();
            return (X509Certificate) keystore.getCertificate(alias);

        } catch (Exception e) {
            throw new KeystoreException("Failed to load certificate: " + e.getMessage(), e);
        }
    }

    private KeyStore loadOrCreateKeystore() throws Exception {
        KeyStore keystore = KeyStore.getInstance(KEYSTORE_TYPE);

        try (FileInputStream fis = new FileInputStream(KEYSTORE_PATH)) {
            keystore.load(fis, getKeystorePassword().toCharArray());
        } catch (IOException e) {
            // Keystore doesn't exist, create new one
            keystore.load(null, null);
        }

        return keystore;
    }

    private void saveKeystore(KeyStore keystore) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(KEYSTORE_PATH)) {
            keystore.store(fos, getKeystorePassword().toCharArray());
        }
    }

    private String getKeystorePassword() {
        return "changeit"; // In production, use proper configuration
    }

    private String generateRandomPassword() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String encryptPassword(String password) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec key = new SecretKeySpec(MASTER_KEY.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(password.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new KeystoreException("Failed to encrypt password", e);
        }
    }

    private String decryptPassword(String encryptedPassword) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec key = new SecretKeySpec(MASTER_KEY.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedPassword));
            return new String(decrypted);
        } catch (Exception e) {
            throw new KeystoreException("Failed to decrypt password", e);
        }
    }
}