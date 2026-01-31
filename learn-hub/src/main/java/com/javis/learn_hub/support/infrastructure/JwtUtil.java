package com.javis.learn_hub.support.infrastructure;

import com.javis.learn_hub.member.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class JwtUtil {

    private static final String ACCESS_TOKEN = "access-token";
    private static final long accessTokenValiditySeconds = 1_000 * 60 * 30 * 30;

    @Value("${jwt.secret-key}")
    private String secretKey;

    public String generateToken(Long userId, Role role) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenValiditySeconds))
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey.getBytes()).parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public Long getMemberId(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey.getBytes())
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }

    public String resolveToken(HttpServletRequest request) {
        if (request == null) {
            throw new IllegalStateException("request is null");
        }
        return extractToken(request.getCookies());
    }

    private String extractToken(Cookie... cookies) {
        if (cookies == null) {
            throw new IllegalStateException("cookies is null");
        }
        return Arrays.stream(cookies)
                .filter(this::isValidAccessToken)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("accessToken is empty"))
                .getValue();
    }

    private boolean isValidAccessToken(Cookie cookie) {
        return cookie.getName().equals(ACCESS_TOKEN);
    }

    public Role extractRole(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey.getBytes())
                .parseClaimsJws(token)
                .getBody();

        String role = claims.get("role", String.class);
        return Role.valueOf(role);
    }

    public String getAccessTokenName() {
        return ACCESS_TOKEN;
    }
}
