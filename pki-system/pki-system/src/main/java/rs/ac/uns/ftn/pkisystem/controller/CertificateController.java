package rs.ac.uns.ftn.pkisystem.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.pkisystem.dto.*;
import rs.ac.uns.ftn.pkisystem.entity.User;
import rs.ac.uns.ftn.pkisystem.security.SecurityUtils;
import rs.ac.uns.ftn.pkisystem.service.CertificateService;

import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/certificates")
@CrossOrigin(origins = {"https://localhost:3000", "https://127.0.0.1:3000", "http://localhost:5173", "https://localhost:5173"})
public class CertificateController {

    @Autowired
    private CertificateService certificateService;

    @PostMapping("/issue")
    @PreAuthorize("hasAnyRole('ADMIN', 'CA_USER')")
    public ResponseEntity<ApiResponse<CertificateDTO>> issueCertificate(@Valid @RequestBody CertificateRequest request) {
        try {
            CertificateDTO certificate = certificateService.issueCertificate(request);
            return ResponseEntity.ok(ApiResponse.success("Certificate issued successfully", certificate));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/upload-csr")
    @PreAuthorize("hasRole('END_USER')")
    public ResponseEntity<ApiResponse<CertificateDTO>> uploadCSR(
            @RequestParam("csr") MultipartFile csrFile,
            @RequestParam("issuerId") Long issuerId,
            @RequestParam("validityDays") Integer validityDays) {
        try {
            if (csrFile.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("CSR file is required"));
            }

            String csrContent = new String(csrFile.getBytes());

            CertificateRequest request = new CertificateRequest();
            request.setCsrData(csrContent);
            request.setIssuerId(issuerId);
            request.setValidityDays(validityDays);
            request.setCertificateType(rs.ac.uns.ftn.pkisystem.entity.CertificateType.END_ENTITY);

            CertificateDTO certificate = certificateService.issueCertificate(request);
            return ResponseEntity.ok(ApiResponse.success("Certificate issued from CSR successfully", certificate));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CA_USER', 'END_USER')")
    public ResponseEntity<ApiResponse<List<CertificateDTO>>> getCertificates() {
        try {
            User currentUser = SecurityUtils.getCurrentUser()
                    .orElseThrow(() -> new SecurityException("User not authenticated"));

            List<CertificateDTO> certificates = certificateService.getCertificatesForUser(currentUser);
            return ResponseEntity.ok(ApiResponse.success("Certificates retrieved successfully", certificates));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CA_USER', 'END_USER')")
    public ResponseEntity<ApiResponse<CertificateDTO>> getCertificate(@PathVariable Long id) {
        try {
            CertificateDTO certificate = certificateService.getCertificateById(id);
            return ResponseEntity.ok(ApiResponse.success("Certificate retrieved successfully", certificate));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/revoke")
    @PreAuthorize("hasAnyRole('ADMIN', 'CA_USER', 'END_USER')")
    public ResponseEntity<ApiResponse<CertificateDTO>> revokeCertificate(
            @PathVariable Long id,
            @Valid @RequestBody RevokeCertificateRequest request) {
        try {
            CertificateDTO certificate = certificateService.revokeCertificate(id, request);
            return ResponseEntity.ok(ApiResponse.success("Certificate revoked successfully", certificate));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'CA_USER', 'END_USER')")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable Long id) {
        try {
            CertificateDTO certificate = certificateService.getCertificateById(id);
            byte[] certData = Base64.getDecoder().decode(certificate.getCertificateData());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", certificate.getCommonName() + ".crt");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(certData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}