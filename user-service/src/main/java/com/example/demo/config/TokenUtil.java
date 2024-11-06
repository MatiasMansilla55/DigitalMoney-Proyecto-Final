package com.example.demo.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class TokenUtil {

    private final String secretKey;
    private static final long EXPIRATION_TIME = 86400000; // 1 día en milisegundos

    // Constructor para inyectar la clave secreta
    public TokenUtil(@Value("${jwt.secret}") String secretKey) {
        this.secretKey = secretKey;
    }

    public String generateToken(Long userId, String email) {
        return Jwts.builder()
                .setSubject(email)
                .setId(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
                .compact();
    }
}

