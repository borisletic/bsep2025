package rs.ac.uns.ftn.pkisystem.service;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.pkisystem.dto.CertificateDTO;
import rs.ac.uns.ftn.pkisystem.dto.CreateCertificateRequest;
import rs.ac.uns.ftn.pkisystem.dto.RevokeCertificateRequest;
import rs.ac.uns.ftn.pkisystem.entity.*;
import rs.ac.uns.ftn.pkisystem.entity.Certificate;
import rs.ac.uns.ftn.pkisystem.exception.ResourceNotFoundException;
import rs.ac.uns.ftn.pkisystem.exception.CertificateGenerationException;
import rs.ac.uns.ftn.pkisystem.repository.CertificateRepository;
import rs.ac.uns.ftn.pkisystem.security.SecurityUtils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CertificateService {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private KeystoreService keystoreService;

    private final SecureRandom secureRandom = new SecureRandom();

    public CertificateDTO createCertificate(CreateCertificateRequest request) {
        try {
            User currentUser = SecurityUtils.getCurrentUser()
                    .orElseThrow(() -> new SecurityException("User not authenticated"));

            Certificate certificate;

            switch (request.getType()) {
                case ROOT:
                    certificate = createRootCertificate(request, currentUser);
                    break;
                case INTERMEDIATE:
                    certificate = createIntermediateCertificate(request, currentUser);
                    break;
                case END_ENTITY:
                    certificate = createEndEntityCertificate(request, currentUser);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid certificate type");
            }

            certificate = certificateRepository.save(certificate);
            auditService.logEvent("CERTIFICATE_CREATED",
                    "Certificate created: " + certificate.getSerialNumber(),
                    "CERTIFICATE", certificate.getId());

            return convertToDTO(certificate);

        } catch (Exception e) {
            throw new CertificateGenerationException("Failed to create certificate: " + e.getMessage(), e);
        }
    }

    private Certificate createRootCertificate(CreateCertificateRequest request, User owner) throws Exception {
        // Generate key pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        // Create certificate
        X500Name subject = new X500Name(request.getSubjectDN());
        BigInteger serialNumber = generateSerialNumber();
        Date validFrom = Date.from(request.getValidFrom().atZone(ZoneId.systemDefault()).toInstant());
        Date validTo = Date.from(request.getValidTo().atZone(ZoneId.systemDefault()).toInstant());

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                subject, serialNumber, validFrom, validTo, subject, keyPair.getPublic());

        // Add extensions for Root CA
        certBuilder.addExtension(Extension.keyUsage, true,
                new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign | KeyUsage.digitalSignature));

        certBuilder.addExtension(Extension.basicConstraints, true,
                new BasicConstraints(true)); // CA certificate

        certBuilder.addExtension(Extension.subjectKeyIdentifier, false,
                new SubjectKeyIdentifier(keyPair.getPublic().getEncoded()));

        // Self-sign the certificate
        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate());
        X509CertificateHolder certHolder = certBuilder.build(signer);
        X509Certificate x509Cert = new JcaX509CertificateConverter().getCertificate(certHolder);

        // Store in keystore
        String alias = "root_" + serialNumber.toString();
        String keystorePassword = keystoreService.storeKeyPair(alias, keyPair, x509Cert);

        // Create certificate entity
        Certificate certificate = new Certificate(
                serialNumber.toString(),
                request.getSubjectDN(),
                request.getSubjectDN(), // Self-signed
                request.getValidFrom(),
                request.getValidTo(),
                CertificateType.ROOT
        );

        certificate.setOwner(owner);
        certificate.setCertificateData(Base64.getEncoder().encodeToString(x509Cert.getEncoded()));
        certificate.setKeystoreAlias(alias);
        certificate.setKeystorePassword(keystorePassword);

        return certificate;
    }

    private Certificate createIntermediateCertificate(CreateCertificateRequest request, User owner) throws Exception {
        // Get issuer certificate
        Certificate issuerCert = certificateRepository.findById(request.getIssuerId())
                .orElseThrow(() -> new ResourceNotFoundException("Issuer certificate not found"));

        if (!issuerCert.isValid()) {
            throw new IllegalArgumentException("Issuer certificate is not valid");
        }

        // Load issuer's private key
        KeyPair issuerKeyPair = keystoreService.loadKeyPair(issuerCert.getKeystoreAlias(),
                issuerCert.getKeystorePassword());

        // Generate key pair for new certificate
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        // Create certificate
        X500Name issuer = new X500Name(issuerCert.getSubjectDN());
        X500Name subject = new X500Name(request.getSubjectDN());
        BigInteger serialNumber = generateSerialNumber();
        Date validFrom = Date.from(request.getValidFrom().atZone(ZoneId.systemDefault()).toInstant());
        Date validTo = Date.from(request.getValidTo().atZone(ZoneId.systemDefault()).toInstant());

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                issuer, serialNumber, validFrom, validTo, subject, keyPair.getPublic());

        // Add extensions for Intermediate CA
        certBuilder.addExtension(Extension.keyUsage, true,
                new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign | KeyUsage.digitalSignature));

        BasicConstraints basicConstraints = request.getPathLengthConstraint() != null ?
                new BasicConstraints(request.getPathLengthConstraint()) :
                new BasicConstraints(true);
        certBuilder.addExtension(Extension.basicConstraints, true, basicConstraints);

        certBuilder.addExtension(Extension.subjectKeyIdentifier, false,
                new SubjectKeyIdentifier(keyPair.getPublic().getEncoded()));

        // Sign with issuer's private key
        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA").build(issuerKeyPair.getPrivate());
        X509CertificateHolder certHolder = certBuilder.build(signer);
        X509Certificate x509Cert = new JcaX509CertificateConverter().getCertificate(certHolder);

        // Store in keystore
        String alias = "intermediate_" + serialNumber.toString();
        String keystorePassword = keystoreService.storeKeyPair(alias, keyPair, x509Cert);

        // Create certificate entity
        Certificate certificate = new Certificate(
                serialNumber.toString(),
                request.getSubjectDN(),
                issuerCert.getSubjectDN(),
                request.getValidFrom(),
                request.getValidTo(),
                CertificateType.INTERMEDIATE
        );

        certificate.setOwner(owner);
        certificate.setIssuer(issuerCert);
        certificate.setCertificateData(Base64.getEncoder().encodeToString(x509Cert.getEncoded()));
        certificate.setKeystoreAlias(alias);
        certificate.setKeystorePassword(keystorePassword);

        return certificate;
    }

    private Certificate createEndEntityCertificate(CreateCertificateRequest request, User owner) throws Exception {
        // Get issuer certificate
        Certificate issuerCert = certificateRepository.findById(request.getIssuerId())
                .orElseThrow(() -> new ResourceNotFoundException("Issuer certificate not found"));

        if (!issuerCert.isValid()) {
            throw new IllegalArgumentException("Issuer certificate is not valid");
        }

        // Load issuer's private key
        KeyPair issuerKeyPair = keystoreService.loadKeyPair(issuerCert.getKeystoreAlias(),
                issuerCert.getKeystorePassword());

        KeyPair keyPair;
        PublicKey publicKey;

        if (request.getCsrData() != null && !request.getCsrData().isEmpty()) {
            // Use CSR
            byte[] csrBytes = Base64.getDecoder().decode(request.getCsrData());
            PKCS10CertificationRequest csr = new PKCS10CertificationRequest(csrBytes);
            JcaPKCS10CertificationRequest jcaCsr = new JcaPKCS10CertificationRequest(csr);
            publicKey = jcaCsr.getPublicKey();
            keyPair = null; // We don't store the private key for end-entity certificates
        } else {
            // Generate new key pair
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            keyPair = keyGen.generateKeyPair();
            publicKey = keyPair.getPublic();
        }

        // Create certificate
        X500Name issuer = new X500Name(issuerCert.getSubjectDN());
        X500Name subject = new X500Name(request.getSubjectDN());
        BigInteger serialNumber = generateSerialNumber();
        Date validFrom = Date.from(request.getValidFrom().atZone(ZoneId.systemDefault()).toInstant());
        Date validTo = Date.from(request.getValidTo().atZone(ZoneId.systemDefault()).toInstant());

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                issuer, serialNumber, validFrom, validTo, subject, publicKey);

        // Add extensions for End Entity
        certBuilder.addExtension(Extension.keyUsage, true,
                new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));

        certBuilder.addExtension(Extension.basicConstraints, true,
                new BasicConstraints(false)); // Not a CA certificate

        certBuilder.addExtension(Extension.subjectKeyIdentifier, false,
                new SubjectKeyIdentifier(publicKey.getEncoded()));

        // Sign with issuer's private key
        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA").build(issuerKeyPair.getPrivate());
        X509CertificateHolder certHolder = certBuilder.build(signer);
        X509Certificate x509Cert = new JcaX509CertificateConverter().getCertificate(certHolder);

        // Store in keystore (certificate only, no private key)
        String alias = "endentity_" + serialNumber.toString();
        keystoreService.storeCertificate(alias, x509Cert);

        // Create certificate entity
        Certificate certificate = new Certificate(
                serialNumber.toString(),
                request.getSubjectDN(),
                issuerCert.getSubjectDN(),
                request.getValidFrom(),
                request.getValidTo(),
                CertificateType.END_ENTITY
        );

        certificate.setOwner(owner);
        certificate.setIssuer(issuerCert);
        certificate.setCertificateData(Base64.getEncoder().encodeToString(x509Cert.getEncoded()));
        certificate.setKeystoreAlias(alias);

        return certificate;
    }

    public List<CertificateDTO> getCertificatesForCurrentUser() {
        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));

        List<Certificate> certificates;
        if (currentUser.getRole() == Role.ADMIN) {
            certificates = certificateRepository.findAll();
        } else {
            certificates = certificateRepository.findCertificatesAccessibleByUser(currentUser);
        }

        return certificates.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CertificateDTO getCertificateById(Long id) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found"));

        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));

        // Check access permissions
        if (!canAccessCertificate(currentUser, certificate)) {
            throw new SecurityException("Access denied to certificate");
        }

        return convertToDTO(certificate);
    }

    public byte[] downloadCertificate(Long id) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found"));

        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));

        if (!canAccessCertificate(currentUser, certificate)) {
            throw new SecurityException("Access denied to certificate");
        }

        auditService.logEvent("CERTIFICATE_DOWNLOADED",
                "Certificate downloaded: " + certificate.getSerialNumber(),
                "CERTIFICATE", certificate.getId());

        return Base64.getDecoder().decode(certificate.getCertificateData());
    }

    public CertificateDTO revokeCertificate(Long id, RevokeCertificateRequest request) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found"));

        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));

        if (!canRevokeCertificate(currentUser, certificate)) {
            throw new SecurityException("Access denied to revoke certificate");
        }

        if (certificate.getStatus() != CertificateStatus.ACTIVE) {
            throw new IllegalArgumentException("Certificate is already revoked or expired");
        }

        certificate.setStatus(CertificateStatus.REVOKED);
        certificate.setRevocationReason(request.getReason());
        certificate.setRevokedAt(LocalDateTime.now());

        certificate = certificateRepository.save(certificate);

        auditService.logEvent("CERTIFICATE_REVOKED",
                "Certificate revoked: " + certificate.getSerialNumber() + ", Reason: " + request.getReason(),
                "CERTIFICATE", certificate.getId());

        return convertToDTO(certificate);
    }

    public List<CertificateDTO> getCACertificates() {
        List<Certificate> caCertificates = certificateRepository.findCACertificates();
        return caCertificates.stream()
                .filter(cert -> cert.getStatus() == CertificateStatus.ACTIVE)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private boolean canAccessCertificate(User user, Certificate certificate) {
        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        if (user.equals(certificate.getOwner())) {
            return true;
        }

        // CA users can access certificates in their chain
        if (user.getRole() == Role.CA_USER) {
            return isInUserChain(user, certificate);
        }

        return false;
    }

    private boolean canRevokeCertificate(User user, Certificate certificate) {
        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        if (user.equals(certificate.getOwner())) {
            return true;
        }

        return false;
    }

    private boolean isInUserChain(User user, Certificate certificate) {
        if (certificate.getOwner().equals(user)) {
            return true;
        }

        Certificate issuer = certificate.getIssuer();
        while (issuer != null) {
            if (issuer.getOwner().equals(user)) {
                return true;
            }
            issuer = issuer.getIssuer();
        }

        return false;
    }

    private BigInteger generateSerialNumber() {
        return new BigInteger(64, secureRandom);
    }

    private CertificateDTO convertToDTO(Certificate certificate) {
        CertificateDTO dto = new CertificateDTO();
        dto.setId(certificate.getId());
        dto.setSerialNumber(certificate.getSerialNumber());
        dto.setSubjectDN(certificate.getSubjectDN());
        dto.setIssuerDN(certificate.getIssuerDN());
        dto.setValidFrom(certificate.getValidFrom());
        dto.setValidTo(certificate.getValidTo());
        dto.setType(certificate.getType());
        dto.setStatus(certificate.getStatus());
        dto.setCertificateData(certificate.getCertificateData());
        dto.setOwner(userService.convertToDTO(certificate.getOwner()));
        dto.setIssuerId(certificate.getIssuer() != null ? certificate.getIssuer().getId() : null);
        dto.setRevocationReason(certificate.getRevocationReason());
        dto.setRevokedAt(certificate.getRevokedAt());
        dto.setCreatedAt(certificate.getCreatedAt());
        return dto;
    }
}