package com.mykcc.login_registrations.Security;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationInMs;

    // Convert the secret string to a SecretKey
    private SecretKey getSecretKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, "HmacSHA512");
    }

    // Generate JWT token
    public String generateToken(Authentication authentication) {
        String email = authentication.getName();  // Extracting email from authentication
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSecretKey())  // Signing with the secure SecretKey
                .compact();
    }

    // Validate the JWT token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())  // Use the secure SecretKey for validation
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            System.out.println("Invalid JWT signature");
        } catch (MalformedJwtException e) {
            System.out.println("Invalid JWT token");
        } catch (ExpiredJwtException e) {
            System.out.println("Expired JWT token");
        } catch (UnsupportedJwtException e) {
            System.out.println("Unsupported JWT token");
        } catch (IllegalArgumentException e) {
            System.out.println("JWT claims string is empty.");
        }
        return false;
    }

    // Extract email (subject) from the JWT token
    public String getUserEmailFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    // Extract any claim from the token
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extract all claims from the token
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())  // Use the secure SecretKey for extraction
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}