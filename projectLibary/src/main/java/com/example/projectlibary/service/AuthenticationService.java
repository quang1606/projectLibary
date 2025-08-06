package com.example.projectlibary.service;

import com.example.projectlibary.dto.reponse.LoginResponse;
import com.example.projectlibary.dto.reponse.RefreshTokenResponse;
import com.example.projectlibary.dto.reponse.UserResponse;
import com.example.projectlibary.dto.request.ForgotPasswordRequest;
import com.example.projectlibary.dto.request.LoginRequest;
import com.example.projectlibary.dto.request.RegistrationRequest;
import com.example.projectlibary.dto.request.RestPasswordRequest;
import com.example.projectlibary.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;

public interface AuthenticationService {
     LoginResponse login(HttpServletResponse response, LoginRequest loginRequest);
     void logout(HttpServletResponse response, HttpServletRequest request);

     RefreshTokenResponse refreshToken(String refreshToken) throws BadRequestException;

    UserResponse register(RegistrationRequest request, HttpServletRequest response);

    void createVerificationTokenForUser(User user, String token);

    String validateVerificationToken(String token);


    void forgotPassword(@Valid ForgotPasswordRequest forgotPasswordRequest, HttpServletRequest request);

    void restPassword(@Valid RestPasswordRequest restPasswordRequest,String token);
}
