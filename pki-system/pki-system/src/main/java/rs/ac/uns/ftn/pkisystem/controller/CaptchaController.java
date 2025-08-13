package rs.ac.uns.ftn.pkisystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.pkisystem.dto.ApiResponse;
import rs.ac.uns.ftn.pkisystem.service.CaptchaService;

@RestController
@RequestMapping("/api/captcha")
@CrossOrigin(origins = {"https://localhost:3000", "https://127.0.0.1:3000", "http://localhost:5173", "https://localhost:5173"})
public class CaptchaController {

    @Autowired
    private CaptchaService captchaService;

    @GetMapping("/generate")
    public ResponseEntity<ApiResponse<CaptchaService.CaptchaChallenge>> generateCaptcha() {
        try {
            CaptchaService.CaptchaChallenge challenge = captchaService.generateChallenge();
            return ResponseEntity.ok(ApiResponse.success("Captcha generated successfully", challenge));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}