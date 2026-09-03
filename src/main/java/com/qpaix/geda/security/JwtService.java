package com.qpaix.geda.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    public static final String CLAIM_TYPE = "type";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_ORG_UNIT_ID = "orgUnitId";
    public static final String TOKEN_TYPE_ACCESS = "ACCESS";
    public static final String TOKEN_TYPE_PENDING_MFA = "PENDING_MFA";

    private final SecretKey signingKey;
    private final long accessTtlMinutes;
    private final long pendingTtlMinutes;

    public JwtService(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.access-ttl-minutes}") long accessTtlMinutes,
                       @Value("${jwt.pending-ttl-minutes}") long pendingTtlMinutes) {
        // Ensure the key material is long enough for HS256 (>= 256 bits) regardless of
        // how short the configured secret is, while staying deterministic per secret value.
        byte[] rawKeyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (rawKeyBytes.length < 32) {
            byte[] padded = new byte[32];
            for (int i = 0; i < 32; i++) {
                padded[i] = rawKeyBytes[i % rawKeyBytes.length];
            }
            rawKeyBytes = padded;
        }
        this.signingKey = Keys.hmacShaKeyFor(rawKeyBytes);
        this.accessTtlMinutes = accessTtlMinutes;
        this.pendingTtlMinutes = pendingTtlMinutes;
    }

    public String issueAccessToken(Long userId, List<String> roles, Long orgUnitId) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(accessTtlMinutes * 60);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_TYPE, TOKEN_TYPE_ACCESS)
                .claim(CLAIM_ROLES, roles)
                .claim(CLAIM_ORG_UNIT_ID, orgUnitId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    public String issuePendingMfaToken(Long userId) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(pendingTtlMinutes * 60);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_TYPE, TOKEN_TYPE_PENDING_MFA)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration != null && expiration.before(new Date());
    }

    public String getTokenType(Claims claims) {
        return claims.get(CLAIM_TYPE, String.class);
    }

    public Long getUserId(Claims claims) {
        return Long.valueOf(claims.getSubject());
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(Claims claims) {
        Object raw = claims.get(CLAIM_ROLES);
        if (raw instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of();
    }

    public Long getOrgUnitId(Claims claims) {
        Object raw = claims.get(CLAIM_ORG_UNIT_ID);
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(raw.toString());
    }

    public static class ExpiredTokenException extends RuntimeException {
        public ExpiredTokenException(String message) {
            super(message);
        }
    }

    public Claims parseClaimsOrThrowExpired(String token) {
        try {
            return parseClaims(token);
        } catch (ExpiredJwtException e) {
            throw new ExpiredTokenException("Token has expired");
        }
    }
}
