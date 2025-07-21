package garabu.garabuServer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import garabu.garabuServer.domain.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;

/**
 * 회원 정보 전송 객체
 * LazyInitializationException 방지를 위한 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 정보 DTO")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "회원 ID", example = "1")
    private Long id;
    
    @Schema(description = "사용자명 (로그인 ID)", example = "user123")
    private String username;
    
    @Schema(description = "이메일", example = "user@example.com")
    private String email;
    
    @Schema(description = "이름", example = "홍길동")
    private String name;
    
    @Schema(description = "OAuth2 제공자 ID", example = "google_123456")
    private String providerId;
    
    @Schema(description = "시스템 역할", example = "ROLE_USER")
    private String role;
    
    /**
     * Member 엔티티를 MemberDTO로 변환
     */
    public static MemberDTO from(Member member) {
        if (member == null) return null;
        
        return MemberDTO.builder()
                .id(member.getId())
                .username(member.getUsername())
                .email(member.getEmail())
                .name(member.getName())
                .providerId(member.getProviderId())
                .role(member.getRole())
                .build();
    }
    
    /**
     * 간단한 정보만 포함하는 Simple DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "회원 간단 정보 DTO")
    public static class SimpleMemberDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @Schema(description = "회원 ID", example = "1")
        private Long id;
        
        @Schema(description = "이름", example = "홍길동")
        private String name;
        
        @Schema(description = "이메일", example = "user@example.com")
        private String email;
        
        public static SimpleMemberDTO from(Member member) {
            if (member == null) return null;
            
            return SimpleMemberDTO.builder()
                    .id(member.getId())
                    .name(member.getName())
                    .email(member.getEmail())
                    .build();
        }
    }
}