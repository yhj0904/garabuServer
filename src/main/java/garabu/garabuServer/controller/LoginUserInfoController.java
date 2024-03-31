package garabu.garabuServer.controller;

import garabu.garabuServer.domain.Member;
import garabu.garabuServer.dto.LoginUserDTO;
import garabu.garabuServer.repository.MemberJPARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LoginUserInfoController {

    private final MemberJPARepository memberJPARepository;

    @GetMapping("/user/me")
    public LoginUserDTO currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        System.out.println(currentUsername);
        System.out.println(currentUsername);
        System.out.println(currentUsername);
        System.out.println(currentUsername);
        System.out.println(currentUsername);
        System.out.println(currentUsername);
        System.out.println(currentUsername);
        System.out.println(currentUsername);
        System.out.println(currentUsername);
        System.out.println(currentUsername);


        Member member = memberJPARepository.findByUsername(currentUsername);

        LoginUserDTO userDto = new LoginUserDTO();
        userDto.setUsername(member.getUsername());
        userDto.setEmail(member.getEmail()); // 실제 Member 엔티티에 이메일이 있다고 가정
        // userDto의 다른 필드를 필요에 따라 설정

        return userDto;
    }
}