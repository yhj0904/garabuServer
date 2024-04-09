package garabu.garabuServer.controller;

import ch.qos.logback.core.model.Model;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.dto.LoginUserDTO;
import garabu.garabuServer.jwt.CustomUserDetails;
import garabu.garabuServer.repository.MemberJPARepository;
import garabu.garabuServer.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LoginUserInfoController {

    private final MemberService memberService;

    @GetMapping("/user/me")
    public LoginUserDTO currentUser() {
        return memberService.getCurrentLoginUserDTO();
    }

        //현재 로그인 한 유저 정보 빼와야함. 근데 리프레쉬 토큰에 있는 유저로 검색이됨.
}
