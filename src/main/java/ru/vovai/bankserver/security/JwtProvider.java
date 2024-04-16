package ru.vovai.bankserver.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import ru.vovai.bankserver.exception.TokenValidationException;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtProvider {

    private static final String SECRET_KEY = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";
    private static final long EXPIRATION_TIME_MS = 24 * 60 * 60 * 1000; // 24 часа

    // Метод для создания JWT токена
    public static String generateToken(String username) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + EXPIRATION_TIME_MS);
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(key)
                .compact();
    }

    // Метод для валидации JWT токена
    public static void validateToken(String token) throws TokenValidationException {
        try {
            Jwts.parserBuilder().setSigningKey(SECRET_KEY.getBytes()).build().parseClaimsJws(token);
        } catch (Exception e) {
            throw new TokenValidationException("Error validating token");
        }
    }

    // Метод для извлечения имени пользователя из JWT токена
    public static String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(SECRET_KEY.getBytes()).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }
}
