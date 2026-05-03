package com.Nook.backend.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    // @Value reads from application.properties
    // so jwt.secret in properties becomes the secretString field here
    @Value("${jwt.secret}")
    private String secretString;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    // Converts the secret string into a cryptographic key object
    // that the JWT library can use for signing
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
    }

    // Creates a new JWT token for a given userId
    // This runs after a successful login or registration
    public String generateToken(String userId) {
        return Jwts.builder()
                .subject(userId)                          // "sub" claim — who this token is for
                .issuedAt(new Date())                     // "iat" — when it was created
                .expiration(new Date(System.currentTimeMillis() + expirationMs)) // "exp"
                .signWith(getSigningKey())                // sign with our secret key
                .compact();                               // build the final string
    }

    // Reads the userId out of a token
    // Called on every request to know who is making it
    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }

    // Checks if the token is valid:
    //   1. Was it signed with our key? (catches fakes/tampering)
    //   2. Has it expired?
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // Parses the token and returns its claims (the payload data)
    // Throws an exception if the signature is invalid or the token is malformed
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}