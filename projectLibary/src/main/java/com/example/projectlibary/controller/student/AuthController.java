package com.example.projectlibary.controller.student;

import com.example.projectlibary.dto.reponse.LoginResponse;
import com.example.projectlibary.dto.reponse.RefreshTokenResponse;
import com.example.projectlibary.dto.reponse.ResponseData;
import com.example.projectlibary.dto.reponse.UserResponse;
import com.example.projectlibary.dto.request.LoginRequest;
import com.example.projectlibary.dto.request.RegistrationRequest;
import com.example.projectlibary.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")

public class AuthController {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        return ResponseEntity.ok(authenticationService.login(response,loginRequest));
    }
    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@CookieValue(name = "refresh_token") String refreshToken) throws BadRequestException {
        RefreshTokenResponse refreshTokenResponse = authenticationService.refreshToken(refreshToken);
        return ResponseEntity.ok(refreshTokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        authenticationService.logout(response,request);
        return ResponseEntity.ok("Logged out successfully.");
    }
    @PostMapping("/registration")
    public ResponseEntity<ResponseData<UserResponse>> register(@RequestBody RegistrationRequest registrationRequest, HttpServletRequest request) {
       UserResponse userResponse= authenticationService.register(registrationRequest,request);
       ResponseData<UserResponse> data = new ResponseData<>(200,"success",userResponse);
       return ResponseEntity.ok(data);

    }

    @GetMapping("/registrationConfirm")
    public ResponseEntity<String> confirmRegistration(@RequestParam("token") String token) {
        String result = authenticationService.validateVerificationToken(token);
        if ("valid".equals(result)) {
            return ResponseEntity.ok("Account activated successfully.");
        }
        return ResponseEntity.badRequest().body(result); // Trả về "invalidToken" hoặc "expiredToken"
    }

}
