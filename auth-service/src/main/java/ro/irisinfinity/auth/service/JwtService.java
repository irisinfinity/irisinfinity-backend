package ro.irisinfinity.auth.service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ro.irisinfinity.auth.config.JwtProperties;

@Service
@AllArgsConstructor
public class JwtService {

    private JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    public String generateToken(final String username) {
        return generateToken(username, Map.of());
    }

    public String generateToken(final String username, final Map<String, Object> extraClaims) {
        return Jwts.builder()
            .claims(extraClaims)
            .subject(username)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration()))
            .signWith(getSigningKey())
            .compact();
    }

    public <T> T extractClaim(final String token, final Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);

        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(final String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public boolean isTokenValid(final String token) {
        return !isTokenExpired(token);
    }

    public boolean isTokenValid(final String token, final String username) {
        final String extractedUsername = extractUsername(token);

        return (extractedUsername.equals(username)) && !isTokenExpired(token);
    }

    public String extractUsername(final String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private boolean isTokenExpired(final String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(final String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}