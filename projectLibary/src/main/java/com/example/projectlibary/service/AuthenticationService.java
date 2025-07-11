package com.example.projectlibary.service;

import com.example.projectlibary.dto.reponse.LoginResponse;
import com.example.projectlibary.dto.reponse.RefreshTokenResponse;
import com.example.projectlibary.dto.request.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.coyote.BadRequestException;

public interface AuthenticationService {
     LoginResponse login(HttpServletResponse response, LoginRequest loginRequest);
     void logout(HttpServletResponse response, HttpServletRequest request);

     RefreshTokenResponse refreshToken(String refreshToken) throws BadRequestException;
}
