package com.dentro.dentrohills.security.jwt;

import com.dentro.dentrohills.security.user.HotelUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication; // Correct import
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${auth.token.jwtSecret}")
    private String jwtSecret;

    @Value("${auth.token.expirationInMils}")
    private int jwtExpirationMs;

    // Generate JWT token for user
    public String generateJwtTokenForUser(Authentication authentication){
        HotelUserDetails userPrincipal = (HotelUserDetails) authentication.getPrincipal();
        List<String> roles = userPrincipal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority).toList();
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Ensure that the key is strong enough for HS256
    private Key key() {
        // Ensure the jwtSecret is properly set and not empty
        /*if (jwtSecret == null || jwtSecret.isEmpty()) {
            logger.error("JWT secret is null or empty");
            throw new IllegalStateException("JWT secret is not configured properly.");
        }

        try {
            // Use the URL-safe base64 decoder to decode the secret
            byte[] decodedKey = Decoders.BASE64URL.decode(jwtSecret);

            // Print out the decoded key length for debugging purposes
            logger.info("Decoded key length: {}", decodedKey.length);

            // Check if the decoded key has at least 32 bytes (for HS256)
            if (decodedKey.length < 32) {
                logger.error("JWT secret is too short! It needs to be at least 32 bytes (256 bits) for HS256.");
                throw new IllegalArgumentException("JWT secret must be at least 32 bytes (256 bits) for HS256.");
            }

            // Return the key for HMAC SHA256
            return Keys.hmacShaKeyFor(decodedKey);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid base64-encoded JWT secret: {}", e.getMessage());
            throw new IllegalStateException("Invalid base64-encoded JWT secret.", e);
        }*/
        return Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(jwtSecret));
    }


    // Extract username from the JWT token
    public String getUserNameFromToken(String token){
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    // Validate JWT token
    public boolean validateToken(String token){
        try{
            Jwts.parserBuilder().setSigningKey(key()).build().parse(token);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {} ", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Expired token: {} ", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("This token is not supported: {} ", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("No claims found: {} ", e.getMessage());
        }
        return false;
    }
}
