package garabu.garabuServer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 로그인한 사용자 정보 DTO
 *
 * 로그인한 사용자의 사용자명과 이메일 정보를 담는 데이터 전송 객체입니다.
 */
@Getter @Setter
@Schema(description = "로그인한 사용자 정보 DTO")
public class LoginUserDTO {
    @Schema(description = "사용자 ID", example = "1")
    private Long id;
    
    @Schema(description = "사용자명", example = "hong123")
    private String username;
    
    @Schema(description = "실제 이름", example = "홍길동")
    private String name;
    
    @Schema(description = "이메일", example = "hong@example.com")
    private String email;
    
    @Schema(description = "사용자 권한", example = "ROLE_USER")
    private String role;
}
