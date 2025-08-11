package rs.ac.uns.ftn.pkisystem.service;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.pkisystem.dto.CertificateDTO;
import rs.ac.uns.ftn.pkisystem.dto.CertificateRequest;
import rs.ac.uns.ftn.pkisystem.dto.RevokeCertificateRequest;
import rs.ac.uns.ftn.pkisystem.entity.*;
import rs.ac.uns.ftn.pkisystem.exception.CertificateOperationException;
import rs.ac.uns.ftn.pkisystem.exception.ResourceNotFoundException;
import rs.ac.uns.ftn.pkisystem.repository.CertificateRepository;
import rs.ac.uns.ftn.pkisystem.security.SecurityUtils;

import java.io.StringReader;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CertificateService {

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private KeystoreManagerService keystoreManagerService;

    @Autowired
    private AuditService auditService;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public CertificateDTO issueCertificate(CertificateRequest request) {
        try {
            User currentUser = SecurityUtils.getCurrentUser()
                    .orElseThrow(() -> new SecurityException("User not authenticated"));

            // Validate request
            validateCertificateRequest(request, currentUser);

            Certificate issuer = null;
            PrivateKey issuerPrivateKey = null;

            // For non-root certificates, get issuer
            if (request.getCertificateType() != CertificateType.ROOT) {
                issuer = certificateRepository.findById(request.getIssuerId())
                        .orElseThrow(() -> new ResourceNotFoundException("Issuer certificate not found"));

                validateIssuerCertificate(issuer, currentUser);
                issuerPrivateKey = keystoreManagerService.getPrivateKey(issuer);
            }

            Certificate certificate;

            if (request.getCertificateType() == CertificateType.END_ENTITY && request.getCsrData() != null) {
                certificate = issueFromCSR(request, issuer, issuerPrivateKey, currentUser);
            } else {
                certificate = issueNewCertificate(request, issuer, issuerPrivateKey, currentUser);
            }

            auditService.logEvent("CERTIFICATE_ISSUED",
                    "Certificate issued: " + certificate.getCommonName(),
                    "CERTIFICATE", certificate.getId());

            return convertToDTO(certificate);

        } catch (Exception e) {
            auditService.logSecurityEvent("CERTIFICATE_ISSUANCE_FAILED",
                    "Failed to issue certificate: " + e.getMessage(), false, e.getMessage());
            throw new CertificateOperationException("Failed to issue certificate: " + e.getMessage(), e);
        }
    }

    private Certificate issueFromCSR(CertificateRequest request, Certificate issuer,
                                     PrivateKey issuerPrivateKey, User currentUser) throws Exception {

        // Parse CSR
        PEMParser pemParser = new PEMParser(new StringReader(request.getCsrData()));
        PKCS10CertificationRequest csr = (PKCS10CertificationRequest) pemParser.readObject();
        pemParser.close();

        // Validate CSR
        if (!csr.isSignatureValid(new org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder().build(csr.getSubjectPublicKeyInfo()))) {
            throw new CertificateOperationException("Invalid CSR signature");
        }

        // Build certificate
        BigInteger serialNumber = generateSerialNumber();
        LocalDateTime validFrom = LocalDateTime.now();
        LocalDateTime validTo = validFrom.plusDays(request.getValidityDays());

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                issuer != null ? new X500Name(issuer.getCertificateData()) : csr.getSubject(),
                serialNumber,
                Date.from(validFrom.atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(validTo.atZone(ZoneId.systemDefault()).toInstant()),
                csr.getSubject(),
                org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter.getPublicKey(csr.getSubjectPublicKeyInfo())
        );

        // Add extensions
        addCertificateExtensions(certBuilder, request);

        // Sign certificate
        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA")
                .setProvider("BC")
                .build(issuerPrivateKey != null ? issuerPrivateKey : generateKeyPair().getPrivate());

        X509CertificateHolder certHolder = certBuilder.build(signer);
        X509Certificate x509Cert = new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(certHolder);

        // Create and save certificate entity
        Certificate certificate = new Certificate();
        populateCertificateEntity(certificate, request, x509Cert, issuer, currentUser);
        certificate.setPublicKey(Base64.getEncoder().encodeToString(
                csr.getSubjectPublicKeyInfo().getEncoded()));

        return certificateRepository.save(certificate);
    }

    private Certificate issueNewCertificate(CertificateRequest request, Certificate issuer,
                                            PrivateKey issuerPrivateKey, User currentUser) throws Exception {

        // Generate key pair for new certificate
        KeyPair keyPair = generateKeyPair();

        // Build subject name
        X500Name subjectName = buildSubjectName(request);
        X500Name issuerName = issuer != null ?
                new X500Name(issuer.getCertificateData()) : subjectName;

        BigInteger serialNumber = generateSerialNumber();
        LocalDateTime validFrom = LocalDateTime.now();
        LocalDateTime validTo = validFrom.plusDays(request.getValidityDays());

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                issuerName,
                serialNumber,
                Date.from(validFrom.atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(validTo.atZone(ZoneId.systemDefault()).toInstant()),
                subjectName,
                keyPair.getPublic()
        );

        // Add extensions
        addCertificateExtensions(certBuilder, request);

        // Sign certificate
        PrivateKey signingKey = issuerPrivateKey != null ? issuerPrivateKey : keyPair.getPrivate();
        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA")
                .setProvider("BC")
                .build(signingKey);

        X509CertificateHolder certHolder = certBuilder.build(signer);
        X509Certificate x509Cert = new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(certHolder);

        // Create and save certificate entity
        Certificate certificate = new Certificate();
        populateCertificateEntity(certificate, request, x509Cert, issuer, currentUser);
        certificate.setPublicKey(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));

        certificate = certificateRepository.save(certificate);

        // Save to keystore if it's a CA certificate
        if (certificate.isCertificateAuthority()) {
            keystoreManagerService.saveCertificateToKeystore(certificate, keyPair.getPrivate());
        }

        return certificate;
    }

    public CertificateDTO revokeCertificate(Long certificateId, RevokeCertificateRequest request) {
        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));

        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found"));

        // Check permissions
        if (!canAccessCertificate(certificate, currentUser)) {
            throw new SecurityException("Access denied to certificate");
        }

        if (certificate.isRevoked()) {
            throw new IllegalStateException("Certificate is already revoked");
        }

        certificate.setRevoked(true);
        certificate.setRevocationReason(request.getReason());
        certificate.setRevocationDate(LocalDateTime.now());

        certificateRepository.save(certificate);

        auditService.logEvent("CERTIFICATE_REVOKED",
                "Certificate revoked: " + certificate.getCommonName() + ", Reason: " + request.getReason(),
                "CERTIFICATE", certificate.getId());

        return convertToDTO(certificate);
    }

    public List<CertificateDTO> getCertificatesForUser(User user) {
        String userRole = user.getRole().name();
        List<Certificate> certificates = certificateRepository.findAccessibleCertificates(user, userRole);

        return certificates.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CertificateDTO getCertificateById(Long id) {
        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));

        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found"));

        if (!canAccessCertificate(certificate, currentUser)) {
            throw new SecurityException("Access denied to certificate");
        }

        auditService.logEvent("CERTIFICATE_VIEWED",
                "Certificate viewed: " + certificate.getCommonName(),
                "CERTIFICATE", certificate.getId());

        return convertToDTO(certificate);
    }

    // Helper methods

    private void validateCertificateRequest(CertificateRequest request, User currentUser) {
        // Validate user permissions
        if (request.getCertificateType() != CertificateType.END_ENTITY &&
                currentUser.getRole() == Role.END_USER) {
            throw new SecurityException("End users can only request end-entity certificates");
        }

        // Validate validity period
        if (request.getValidityDays() <= 0 || request.getValidityDays() > 3650) {
            throw new IllegalArgumentException("Invalid validity period");
        }
    }

    private void validateIssuerCertificate(Certificate issuer, User currentUser) {
        if (!issuer.isCertificateAuthority()) {
            throw new IllegalArgumentException("Issuer must be a CA certificate");
        }

        if (issuer.isRevoked()) {
            throw new IllegalArgumentException("Cannot use revoked certificate as issuer");
        }

        if (!issuer.isValid()) {
            throw new IllegalArgumentException("Issuer certificate is not valid");
        }

        if (!canAccessCertificate(issuer, currentUser)) {
            throw new SecurityException("Access denied to issuer certificate");
        }
    }

    private boolean canAccessCertificate(Certificate certificate, User user) {
        if (user.getRole() == Role.ADMIN) {
            return true;
        }
        return certificate.getOwner().getId().equals(user.getId());
    }

    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    private BigInteger generateSerialNumber() {
        return BigInteger.valueOf(System.currentTimeMillis());
    }

    private X500Name buildSubjectName(CertificateRequest request) {
        StringBuilder subjectBuilder = new StringBuilder();
        subjectBuilder.append("CN=").append(request.getCommonName());

        if (request.getOrganization() != null) {
            subjectBuilder.append(", O=").append(request.getOrganization());
        }
        if (request.getOrganizationalUnit() != null) {
            subjectBuilder.append(", OU=").append(request.getOrganizationalUnit());
        }
        if (request.getCountry() != null) {
            subjectBuilder.append(", C=").append(request.getCountry());
        }
        if (request.getState() != null) {
            subjectBuilder.append(", ST=").append(request.getState());
        }
        if (request.getLocality() != null) {
            subjectBuilder.append(", L=").append(request.getLocality());
        }

        return new X500Name(subjectBuilder.toString());
    }

    private void addCertificateExtensions(X509v3CertificateBuilder certBuilder, CertificateRequest request) throws Exception {
        // Basic Constraints
        if (request.getCertificateType() != CertificateType.END_ENTITY) {
            boolean isCA = true;
            int pathLength = request.getCertificateType() == CertificateType.ROOT ? -1 : 0;
            certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(isCA));
        }

        // Key Usage
        int keyUsage = 0;
        if (request.getCertificateType() != CertificateType.END_ENTITY) {
            keyUsage |= KeyUsage.keyCertSign | KeyUsage.cRLSign;
        } else {
            keyUsage |= KeyUsage.digitalSignature | KeyUsage.keyEncipherment;
        }
        certBuilder.addExtension(Extension.keyUsage, true, new KeyUsage(keyUsage));
    }

    private void populateCertificateEntity(Certificate certificate, CertificateRequest request,
                                           X509Certificate x509Cert, Certificate issuer, User owner) throws Exception {
        certificate.setSerialNumber(x509Cert.getSerialNumber().toString());
        certificate.setCommonName(request.getCommonName());
        certificate.setOrganization(request.getOrganization());
        certificate.setOrganizationalUnit(request.getOrganizationalUnit());
        certificate.setCountry(request.getCountry());
        certificate.setState(request.getState());
        certificate.setLocality(request.getLocality());
        certificate.setEmail(request.getEmail());
        certificate.setCertificateType(request.getCertificateType());
        certificate.setValidFrom(request.getValidityDays() != null ? LocalDateTime.now() : LocalDateTime.now());
        certificate.setValidTo(LocalDateTime.now().plusDays(request.getValidityDays()));
        certificate.setIssuer(issuer);
        certificate.setOwner(owner);
        certificate.setCertificateData(Base64.getEncoder().encodeToString(x509Cert.getEncoded()));

        // Set extensions as JSON
        certificate.setKeyUsage(request.getKeyUsage());
        certificate.setExtendedKeyUsage(request.getExtendedKeyUsage());
        certificate.setBasicConstraints(request.getBasicConstraints());
        certificate.setSubjectAlternativeNames(request.getSubjectAlternativeNames());
    }

    private CertificateDTO convertToDTO(Certificate certificate) {
        CertificateDTO dto = new CertificateDTO();
        dto.setId(certificate.getId());
        dto.setSerialNumber(certificate.getSerialNumber());
        dto.setCommonName(certificate.getCommonName());
        dto.setOrganization(certificate.getOrganization());
        dto.setOrganizationalUnit(certificate.getOrganizationalUnit());
        dto.setCountry(certificate.getCountry());
        dto.setState(certificate.getState());
        dto.setLocality(certificate.getLocality());
        dto.setEmail(certificate.getEmail());
        dto.setCertificateType(certificate.getCertificateType());
        dto.setValidFrom(certificate.getValidFrom());
        dto.setValidTo(certificate.getValidTo());
        dto.setCreatedAt(certificate.getCreatedAt());
        dto.setRevoked(certificate.isRevoked());
        dto.setRevocationReason(certificate.getRevocationReason());
        dto.setRevocationDate(certificate.getRevocationDate());
        dto.setCertificateData(certificate.getCertificateData());
        dto.setPublicKey(certificate.getPublicKey());

        // Issuer information
        if (certificate.getIssuer() != null) {
            dto.setIssuerId(certificate.getIssuer().getId());
            dto.setIssuerCommonName(certificate.getIssuer().getCommonName());
            dto.setIssuerSerialNumber(certificate.getIssuer().getSerialNumber());
        }

        // Owner information
        dto.setOwnerId(certificate.getOwner().getId());
        dto.setOwnerEmail(certificate.getOwner().getEmail());
        dto.setOwnerName(certificate.getOwner().getFirstName() + " " + certificate.getOwner().getLastName());

        // Extensions
        dto.setKeyUsage(certificate.getKeyUsage());
        dto.setExtendedKeyUsage(certificate.getExtendedKeyUsage());
        dto.setBasicConstraints(certificate.getBasicConstraints());
        dto.setSubjectAlternativeNames(certificate.getSubjectAlternativeNames());

        return dto;
    }
}To(toEmail);
            helper.setSubject("PKI System - Account Activation");
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
// Fallback to simple email
sendSimpleActivationEmail(toEmail, activationToken);
        }
                }

public void sendPasswordResetEmail(String toEmail, String resetToken) {
    try {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        Context context = new Context();
        context.setVariable("resetLink", frontendUrl + "/password-reset/" + resetToken);
        context.setVariable("email", toEmail);

        String htmlContent = templateEngine.process("password-reset-email", context);

        helper.setFrom(fromEmail);
        helper.set