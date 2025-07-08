package garabu.garabuServer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 사용자 정보 전달용 DTO
 *
 * 회원의 역할, 이름, 아이디, 이메일, OAuth2 제공자 ID를 담는 데이터 전송 객체입니다.
 */
@Getter @Setter
@Schema(description = "사용자 정보 DTO")
public class UserDTO {

    @Schema(description = "사용자 역할", example = "USER")
    private String role;
    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;
    @Schema(description = "사용자 아이디", example = "hong123")
    private String username;
    @Schema(description = "사용자 이메일", example = "hong@example.com")
    private String email;
    @Schema(description = "OAuth2 제공자 ID", example = "google_123456")
    private String providerId;
}