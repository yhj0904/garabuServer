package garabu.garabuServer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.RSAPublicKeySpec;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Apple OAuth 처리 서비스
 * 
 * Apple Identity Token 검증 및 사용자 정보 추출을 담당합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppleOAuthService {
    
    @Value("${spring.security.oauth2.client.registration.apple.client-id}")
    private String appleClientId;
    
    @Value("${apple.team-id:}")
    private String appleTeamId;
    
    @Value("${apple.key-id:}")
    private String appleKeyId;
    
    @Value("${apple.private-key:}")
    private String applePrivateKey;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Apple 공개 키 캐시 (1시간 TTL)
    private final Map<String, ApplePublicKey> publicKeyCache = new ConcurrentHashMap<>();
    private long lastCacheUpdate = 0;
    private static final long CACHE_TTL = 3600000; // 1시간

    /**
     * Apple Identity Token에서 사용자 정보 추출
     * 
     * @param identityToken Apple Identity Token (JWT)
     * @return 사용자 정보 Map
     */
    public Map<String, Object> extractUserInfoFromIdentityToken(String identityToken) {
        try {
            // JWT 헤더에서 kid 추출
            String[] tokenParts = identityToken.split("\\.");
            if (tokenParts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT format");
            }
            
            // 헤더 디코딩
            String header = new String(Base64.getUrlDecoder().decode(tokenParts[0]));
            Map<String, Object> headerMap = objectMapper.readValue(header, Map.class);
            String kid = (String) headerMap.get("kid");
            
            if (kid == null) {
                throw new IllegalArgumentException("JWT header missing kid");
            }
            
            // Apple 공개 키 조회
            ApplePublicKey applePublicKey = getApplePublicKey(kid);
            if (applePublicKey == null) {
                throw new IllegalArgumentException("Public key not found for kid: " + kid);
            }
            
            // RSA 공개 키 생성
            PublicKey publicKey = createPublicKey(applePublicKey);
            
            // JWT 검증 및 클레임 추출
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(identityToken)
                    .getBody();
            
            // 추가 검증
            validateClaims(claims);
            
            // 클레임을 Map으로 변환
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("sub", claims.getSubject());
            userInfo.put("email", claims.get("email", String.class));
            userInfo.put("email_verified", claims.get("email_verified", Boolean.class));
            userInfo.put("is_private_email", claims.get("is_private_email", Boolean.class));
            if (claims.get("real_user_status") != null) {
                userInfo.put("real_user_status", claims.get("real_user_status", Integer.class));
            }
            
            return userInfo;
            
        } catch (JwtException e) {
            log.error("JWT 검증 실패: {}", e.getMessage());
            throw new RuntimeException("Apple Identity Token 검증 실패", e);
        } catch (Exception e) {
            log.error("Apple Identity Token 파싱 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Apple Identity Token 처리 실패", e);
        }
    }

    /**
     * Apple 공개 키 조회
     * 
     * @param kid 키 ID
     * @return Apple 공개 키
     */
    private ApplePublicKey getApplePublicKey(String kid) {
        try {
            // 캐시 확인
            if (System.currentTimeMillis() - lastCacheUpdate > CACHE_TTL) {
                publicKeyCache.clear();
            }
            
            if (publicKeyCache.containsKey(kid)) {
                return publicKeyCache.get(kid);
            }
            
            // Apple 공개 키 조회 API 호출
            ResponseEntity<Map> response = restTemplate.exchange(
                "https://appleid.apple.com/auth/keys",
                HttpMethod.GET,
                null,
                Map.class
            );
            
            Map<String, Object> body = response.getBody();
            if (body == null || !body.containsKey("keys")) {
                throw new RuntimeException("Invalid response from Apple");
            }
            
            List<Map<String, Object>> keys = (List<Map<String, Object>>) body.get("keys");
            
            // 모든 키를 캐시에 저장
            for (Map<String, Object> key : keys) {
                ApplePublicKey appleKey = new ApplePublicKey();
                appleKey.kty = (String) key.get("kty");
                appleKey.kid = (String) key.get("kid");
                appleKey.use = (String) key.get("use");
                appleKey.alg = (String) key.get("alg");
                appleKey.n = (String) key.get("n");
                appleKey.e = (String) key.get("e");
                
                publicKeyCache.put(appleKey.kid, appleKey);
            }
            
            lastCacheUpdate = System.currentTimeMillis();
            
            return publicKeyCache.get(kid);
            
        } catch (Exception e) {
            log.error("Apple 공개 키 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Apple 공개 키 조회 실패", e);
        }
    }

    /**
     * RSA 공개 키 생성
     * 
     * @param applePublicKey Apple 공개 키 정보
     * @return RSA 공개 키
     */
    private PublicKey createPublicKey(ApplePublicKey applePublicKey) {
        try {
            byte[] nBytes = Base64.getUrlDecoder().decode(applePublicKey.n);
            byte[] eBytes = Base64.getUrlDecoder().decode(applePublicKey.e);
            
            BigInteger modulus = new BigInteger(1, nBytes);
            BigInteger exponent = new BigInteger(1, eBytes);
            
            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            
            return factory.generatePublic(spec);
        } catch (Exception e) {
            throw new RuntimeException("RSA 공개 키 생성 실패", e);
        }
    }
    
    /**
     * JWT 클레임 검증
     * 
     * @param claims JWT 클레임
     */
    private void validateClaims(Claims claims) {
        // 발급자 검증
        if (!"https://appleid.apple.com".equals(claims.getIssuer())) {
            throw new JwtException("Invalid issuer");
        }
        
        // 대상 검증
        if (!claims.getAudience().contains(appleClientId)) {
            throw new JwtException("Invalid audience");
        }
        
        // 만료 시간 검증 (이미 Jwts.parserBuilder()에서 자동으로 검증됨)
    }
    
    /**
     * Apple 공개 키 데이터 클래스
     */
    @Data
    private static class ApplePublicKey {
        private String kty;
        private String kid;
        private String use;
        private String alg;
        private String n;
        private String e;
    }

    /**
     * Apple 클라이언트 시크릿 생성 (서버 간 통신용)
     * 
     * @return JWT 형태의 클라이언트 시크릿
     */
    public String generateClientSecret() {
        try {
            Date now = new Date();
            Date expiration = new Date(now.getTime() + 3600000); // 1시간 후 만료
            
            return Jwts.builder()
                    .setIssuer(appleTeamId)
                    .setIssuedAt(now)
                    .setExpiration(expiration)
                    .setAudience("https://appleid.apple.com")
                    .setSubject(appleClientId)
                    .setHeaderParam("kid", appleKeyId)
                    .signWith(getPrivateKey(), SignatureAlgorithm.ES256)
                    .compact();
                    
        } catch (Exception e) {
            log.error("Apple 클라이언트 시크릿 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Apple 클라이언트 시크릿 생성 실패", e);
        }
    }

    /**
     * Apple 개인 키 로드
     * 
     * @return 개인 키
     */
    private Key getPrivateKey() {
        // 실제 구현에서는 Apple 개인 키 파일을 로드하여 Key 객체 생성
        // 여기서는 간단한 구현만 제공
        byte[] keyBytes = Base64.getDecoder().decode(applePrivateKey.getBytes());
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.ES256.getJcaName());
    }

    /**
     * Apple 토큰 검증
     * 
     * @param accessToken Apple에서 받은 액세스 토큰
     * @return 토큰 검증 결과
     */
    public boolean validateAppleToken(String accessToken) {
        try {
            // Apple 토큰 검증 API 호출
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", appleClientId);
            body.add("client_secret", generateClientSecret());
            body.add("token", accessToken);
            
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                "https://appleid.apple.com/auth/token",
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            return response.getStatusCode().is2xxSuccessful();
            
        } catch (Exception e) {
            log.error("Apple 토큰 검증 실패: {}", e.getMessage(), e);
            return false;
        }
    }
}