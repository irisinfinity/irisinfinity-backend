package ro.irisinfinity.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import ro.irisinfinity.auth.config.JwtProperties;
import ro.irisinfinity.platform.common.enums.Role;

@Service
@RefreshScope
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties props;

    private SecretKey accessKey;
    private SecretKey refreshKey;

    @PostConstruct
    void initKeys() {
        this.accessKey = keyFrom(props.getSecret());
        this.refreshKey = keyFrom(props.getRefreshSecret());
    }

    private static SecretKey keyFrom(String secret) {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (Exception ignored) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(UUID userId, String subjectEmail, Set<Role> roles) {
        Instant now = Instant.now();
        Duration ttl = props.getAccessTtlMinutes();

        return Jwts.builder()
            .header().type("JWT").and()
            .issuer(props.getIssuer())
            .audience().add(props.getAudience()).and()
            .subject(subjectEmail)
            .id(UUID.randomUUID().toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(ttl)))
            .claims(Map.of("userId", userId, "roles", roles))
            .signWith(accessKey, Jwts.SIG.HS256)
            .compact();
    }

    public String createRefreshToken(UUID userId, String subjectEmail) {
        Instant now = Instant.now();
        Duration ttl = props.getRefreshTtlDays();

        return Jwts.builder()
            .header().type("JWT").and()
            .issuer(props.getIssuer())
            .audience().add(props.getAudience()).and()
            .subject(subjectEmail)
            .id(UUID.randomUUID().toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(ttl)))
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