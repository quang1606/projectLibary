package com.example.projectlibary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String TOKEN_BLACKLIST = "TOKEN_BLACKLIST";

    public void blacklist(String token, long duration) {
        redisTemplate.opsForValue().set(TOKEN_BLACKLIST+token, "blacklisted", duration, TimeUnit.MILLISECONDS);
    }

    public boolean isBlacklisted(String token) {
        return redisTemplate.hasKey(TOKEN_BLACKLIST+token);
    }
}
