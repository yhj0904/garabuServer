package garabu.garabuServer.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JWTUtil {

    private Key key;

    public JWTUtil(@Value("${jwt.secret}")String secret) {

        byte[] byteSecretKey = Decoders.BASE64.decode(secret);
        key = Keys.hmacShaKeyFor(byteSecretKey);
    }

    public String getUsername(String token) {

        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("username", String.class);
    }

    public String getRole(String token) {

        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("role", String.class);
    }

    public String getCategory(String token) {

        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("category", String.class);
    }
    
    public String getJti(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getId();
    }
    
    public Long getAbsoluteExpiration(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claims.get("absExp", Long.class);
    }
    
    public Long getIdleExpiration(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claims.get("idleExp", Long.class);
    }
    
    public Boolean isExpired(String token) {

        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getExpiration().before(new Date());
    }
    
    /**
     * 유휴 만료 및 절대 만료 체크
     * @param token JWT 토큰
     * @return 만료 여부 (true: 만료됨, false: 유효함)
     */
    public Boolean isExpiredWithIdle(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        long now = System.currentTimeMillis();
        
        // 절대 만료 시간 체크
        Date expiration = claims.getExpiration();
        if (expiration != null && expiration.before(new Date())) {
            return true;
        }
        
        // 유휴 만료 시간 체크 (refresh token에만 적용)
        String category = claims.get("category", String.class);
        if (JWTConstants.REFRESH_TOKEN_CATEGORY.equals(category)) {
            Long idleExp = claims.get("idleExp", Long.class);
            if (idleExp != null && idleExp < now) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * JWT 토큰 생성 (기본 메서드 - 하위 호환성 유지)
     */
    public String createJwt(String category, String username, String role, Long expiredMs) {
        return createJwtWithJti(category, username, role, expiredMs, null);
    }
    
    /**
     * JWT 토큰 생성 (jti 포함)
     */
    public String createJwtWithJti(String category, String username, String role, Long expiredMs, String jti) {
        Claims claims = Jwts.claims();
        claims.put("category", category);
        claims.put("username", username);
        claims.put("role", role);
        
        // Refresh Token인 경우 유휴 만료 시간 추가
        long now = System.currentTimeMillis();
        if (JWTConstants.REFRESH_TOKEN_CATEGORY.equals(category)) {
            claims.put("idleExp", now + JWTConstants.IDLE_EXPIRE_TIME);
            claims.put("absExp", now + expiredMs); // 절대 만료 시간
        }
        
        return Jwts.builder()
                .setClaims(claims)
                .setId(jti != null ? jti : UUID.randomUUID().toString()) // jti 설정
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expiredMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}