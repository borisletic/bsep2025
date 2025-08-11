package rs.ac.uns.ftn.pkisystem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.pkisystem.entity.Certificate;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class KeystoreManagerService {

    @Value("${keystore.base.path:keystores/}")
    private String keystoreBasePath;

    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String ENCRYPTION_ALGORITHM = "AES";

    public void saveCertificateToKeystore(Certificate certificate, PrivateKey privateKey) throws Exception {
        // Generate keystore password
        String keystorePassword = generateKeystorePassword();

        // Create keystore
        KeyStore keystore = KeyStore.getInstance(KEYSTORE_TYPE);
        keystore.load(null, null);

        // Convert certificate data from Base64
        byte[] certData = Base64.getDecoder().decode(certificate.getCertificateData());
        java.security.cert.Certificate javaCert =
                java.security.cert.CertificateFactory.getInstance("X.509").generateCertificate(
                        new java.io.ByteArrayInputStream(certData));

        // Add certificate and private key to keystore
        String alias = "cert_" + certificate.getId();
        keystore.setKeyEntry(alias, privateKey, keystorePassword.toCharArray(),
                new java.security.cert.Certificate[]{javaCert});

        // Save keystore to file
        String keystorePath = keystoreBasePath + "keystore_" + certificate.getId() + ".p12";
        Files.createDirectories(Paths.get(keystoreBasePath));

        try (FileOutputStream fos = new FileOutputStream(keystorePath)) {
            keystore.store(fos, keystorePassword.toCharArray());
        }

        // Encrypt and save keystore password
        String encryptedPassword = encryptPassword(keystorePassword, certificate.getOwner().getId());

        // Update certificate with keystore information
        certificate.setKeystorePath(keystorePath);
        certificate.setKeystorePasswordEncrypted(encryptedPassword);
        certificate.setKeystoreAlias(alias);
    }

    public PrivateKey getPrivateKey(Certificate certificate) throws Exception {
        if (certificate.getKeystorePath() == null) {
            throw new IllegalArgumentException("Certificate does not have associated keystore");
        }

        // Decrypt keystore password
        String keystorePassword = decryptPassword(
                certificate.getKeystorePasswordEncrypted(),
                certificate.getOwner().getId());

        // Load keystore
        KeyStore keystore = KeyStore.getInstance(KEYSTORE_TYPE);
        try (FileInputStream fis = new FileInputStream(certificate.getKeystorePath())) {
            keystore.load(fis, keystorePassword.toCharArray());
        }

        // Get private key
        return (PrivateKey) keystore.getKey(certificate.getKeystoreAlias(), keystorePassword.toCharArray());
    }

    private String generateKeystorePassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private String encryptPassword(String password, Long userId) throws Exception {
        SecretKey secretKey = generateUserSpecificKey(userId);
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(password.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private String decryptPassword(String encryptedPassword, Long userId) throws Exception {
        SecretKey secretKey = generateUserSpecificKey(userId);
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedPassword));
        return new String(decryptedBytes);
    }

    private SecretKey generateUserSpecificKey(Long userId) throws Exception {
        // Generate a deterministic key based on user ID
        // In production, this should use a more secure approach
        String keyString = "PKI_SYSTEM_KEY_" + userId.toString();
        byte[] keyBytes = keyString.getBytes();

        // Ensure key is 16 bytes for AES
        byte[] adjustedKey = new byte[16];
        System.arraycopy(keyBytes, 0, adjustedKey, 0, Math.min(keyBytes.length, 16));

        return new SecretKeySpec(adjustedKey, ENCRYPTION_ALGORITHM);
    }
}