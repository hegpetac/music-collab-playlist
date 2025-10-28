package hu.hegpetac.music.collab.playlist.be.authentication.util;

import hu.hegpetac.music.collab.playlist.be.authentication.entity.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {
    private final SecretKey signingKey;

    public JwtUtil() {
        byte[] secret = java.util.Base64.getDecoder().decode(System.getenv("JWT_SECRET"));
        this.signingKey = Keys.hmacShaKeyFor(secret);
    }

    public String generateToken(AppUser user, List<String> roles, long ttlMillis) {
        JwtBuilder builder = Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ttlMillis))
                .signWith(signingKey);
        return builder.compact();
    }

    public Claims validateAndGetClaims(String jwt) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromClaims(Claims claims) {
        Object roles = claims.get("roles");
        if (roles instanceof List) {
            return (List<String>) roles;
        }
        return List.of();
    }
}
