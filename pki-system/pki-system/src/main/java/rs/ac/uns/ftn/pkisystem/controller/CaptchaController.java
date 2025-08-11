package rs.ac.uns.ftn.pkisystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.pkisystem.dto.ApiResponse;
import rs.ac.uns.ftn.pkisystem.dto.CaptchaResponse;
import rs.ac.uns.ftn.pkisystem.dto.CaptchaVerifyRequest;
import rs.ac.uns.ftn.pkisystem.service.CaptchaService;

@RestController
@RequestMapping("/api/captcha")
@CrossOrigin(origins = {"https://localhost:3000", "https://127.0.0.1:3000"})
public class CaptchaController {

    @Autowired
    private CaptchaService captchaService;

    @GetMapping("/generate")
    public ResponseEntity<ApiResponse<CaptchaResponse>> generateCaptcha() {
        try {
            CaptchaResponse response = captchaService.generateCaptcha();
            return ResponseEntity.ok(ApiResponse.success("CAPTCHA generated", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyCaptcha(@RequestBody CaptchaVerifyRequest request) {
        try {
            boolean isValid = captchaService.verifyCaptcha(request.getSessionId(), request.getAnswer());
            return ResponseEntity.ok(ApiResponse.success("CAPTCHA verified", isValid));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}