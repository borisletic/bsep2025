package rs.ac.uns.ftn.pkisystem.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.pkisystem.dto.*;
import rs.ac.uns.ftn.pkisystem.service.CertificateService;

import java.util.List;

@RestController
@RequestMapping("/api/certificates")
@PreAuthorize("hasAnyRole('ADMIN', 'CA_USER', 'END_USER')")
@CrossOrigin(origins = {"https://localhost:3000", "https://127.0.0.1:3000", "http://localhost:5173", "https://localhost:5173"})
public class CertificateController {

    @Autowired
    private CertificateService certificateService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CA_USER')")
    public ResponseEntity<ApiResponse<CertificateDTO>> createCertificate(@Valid @RequestBody CreateCertificateRequest request) {
        try {
            CertificateDTO certificate = certificateService.createCertificate(request);
            return ResponseEntity.ok(ApiResponse.success("Certificate created successfully", certificate));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CertificateDTO>>> getCertificates() {
        try {
            List<CertificateDTO> certificates = certificateService.getCertificatesForCurrentUser();
            return ResponseEntity.ok(ApiResponse.success("Certificates retrieved successfully", certificates));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CertificateDTO>> getCertificate(@PathVariable Long id) {
        try {
            CertificateDTO certificate = certificateService.getCertificateById(id);
            return ResponseEntity.ok(ApiResponse.success("Certificate retrieved successfully", certificate));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable Long id) {
        try {
            byte[] certificateData = certificateService.downloadCertificate(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "certificate_" + id + ".crt");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(certificateData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/revoke")
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

    @GetMapping("/ca")
    @PreAuthorize("hasAnyRole('ADMIN', 'CA_USER')")
    public ResponseEntity<ApiResponse<List<CertificateDTO>>> getCACertificates() {
        try {
            List<CertificateDTO> certificates = certificateService.getCACertificates();
            return ResponseEntity.ok(ApiResponse.success("CA certificates retrieved successfully", certificates));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}