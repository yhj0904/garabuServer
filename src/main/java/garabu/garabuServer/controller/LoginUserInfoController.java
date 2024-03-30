package garabu.garabuServer.controller;

import garabu.garabuServer.dto.LoginUserDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginUserInfoController {
    @GetMapping("/user/me")
    public LoginUserDTO currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        // 현재 사용자의 이메일을 가져오는 로직은 애플리케이션에 따라 달라질 수 있습니다.
        // 예제를 위해 여기서는 username을 이메일로 가정합니다.
        String email = currentUsername + "@example.com";

        LoginUserDTO userDto = new LoginUserDTO();
        userDto.setUsername(currentUsername);
        userDto.setEmail(email);

        return userDto;
    }
}
