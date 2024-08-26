package com.finpro.roomio_backend.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisTokenService {

    private final StringRedisTemplate redisTemplate;

    @Value("${verification.token.expiration:60}") // expiration time in minutes
    private long expirationTime;

    public RedisTokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void storeToken(String token, String value) {
        redisTemplate.opsForValue().set(token, value, expirationTime, TimeUnit.MINUTES);
    }

    public String getToken(String token) {
        return redisTemplate.opsForValue().get(token);
    }

    public void deleteToken(String token) {
        redisTemplate.delete(token);
    }
}