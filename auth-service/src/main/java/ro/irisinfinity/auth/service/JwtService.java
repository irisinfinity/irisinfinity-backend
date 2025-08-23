package ro.irisinfinity.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final String issuer;
    private final String audience;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    public JwtService(
        @Value("${security.jwt.secret}") String accessSecret,
        @Value("${security.jwt.refresh-secret}") String refreshSecret,
        @Value("${security.jwt.issuer}") String issuer,
        @Value("${security.jwt.audience}") String audience,
        @Value("${security.jwt.access-ttl-minutes:15}") long accessTtlMinutes,
        @Value("${security.jwt.refresh-ttl-days:7}") long refreshTtlDays) {

        String accessB64 = base64IfNeeded(accessSecret);
        String refreshB64 = base64IfNeeded(refreshSecret);

        this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessB64));
        this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshB64));
        this.issuer = issuer;
        this.audience = audience;
        this.accessTtl = Duration.ofMinutes(accessTtlMinutes);
        this.refreshTtl = Duration.ofDays(refreshTtlDays);
    }

    private static String base64IfNeeded(String secret) {
        try {
            Decoders.BASE64.decode(secret);
            return secret;
        } catch (Exception ignored) {
            return java.util.Base64.getEncoder()
                .encodeToString(secret.getBytes(StandardCharsets.UTF_8));
        }
    }

    public String createAccessToken(String userId, String subjectEmail) {
        Instant now = Instant.now();
        return Jwts.builder()
            .header().type("JWT").and()
            .issuer(issuer)
            .audience().add(audience).and()
            .subject(subjectEmail)
            .id(UUID.randomUUID().toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(accessTtl)))
            .claims(Map.of("userId", userId))
            .signWith(accessKey, Jwts.SIG.HS256)
            .compact();
    }

    public String createRefreshToken(String userId, String subjectEmail) {
        Instant now = Instant.now();
        return Jwts.builder()
            .header().type("JWT").and()
            .issuer(issuer)
            .audience().add(audience).and()
            .subject(subjectEmail)
            .id(UUID.randomUUID().toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(refreshTtl)))
            .claims(Map.of("userId", userId, "typ", "refresh"))
            .signWith(refreshKey, Jwts.SIG.HS256)
            .compact();
    }

    public Jws<Claims> parseRefreshToken(String token) {
        return Jwts.parser()
            .verifyWith(refreshKey)
            .build()
            .parseSignedClaims(token);
    }
}