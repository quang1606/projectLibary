package com.example.projectlibary.service;

import com.example.projectlibary.dto.reponse.RefreshTokenResponse;
import com.example.projectlibary.model.RefreshToken;

import java.util.Optional;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(String email);
    Optional<RefreshToken> finByToken(String token);
    RefreshToken verifyRefreshToken(RefreshToken refreshToken);
    void deleteByToken(String token);
}
