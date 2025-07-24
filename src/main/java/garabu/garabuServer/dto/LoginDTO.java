package garabu.garabuServer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 로그인 요청 DTO
 *
 * 로그인 시 사용자명과 비밀번호를 전달하는 데이터 전송 객체입니다.
 */
@Getter
@Setter
@Schema(description = "로그인 요청 DTO")
public class LoginDTO {

    @Schema(description = "이메일", example = "user@example.com")
    private String email;
    @Schema(description = "비밀번호", example = "password123")
    private String password;
}