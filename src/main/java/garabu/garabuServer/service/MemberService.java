package garabu.garabuServer.service;

import garabu.garabuServer.domain.Member;
import garabu.garabuServer.dto.CustomOAuth2User;
import garabu.garabuServer.dto.LoginUserDTO;
import garabu.garabuServer.jwt.CustomUserDetails;
import garabu.garabuServer.repository.MemberJPARepository;
import garabu.garabuServer.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService{

    private final MemberRepository memberRepository;

    private final MemberJPARepository memberJPARepository;

    @Transactional
    public Long join (Member member) {
        validateDuplicateMember(member);
        validateDuplicateEmail(member);
        memberRepository.save(member);
        return member.getId();
    }

    public LoginUserDTO getCurrentLoginUserDTO() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomOAuth2User customOAuth2User = (CustomOAuth2User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = customOAuth2User.getName();
        String username1 = customOAuth2User.getUsername();
        String email = customOAuth2User.getEmail();

        System.out.println(username);
        System.out.println(username);
        System.out.println(username);
        System.out.println(username);
        System.out.println(username);
        System.out.println(username);
        System.out.println(username);
        System.out.println(username1);
        System.out.println(username1);
        System.out.println(username1);
        System.out.println(username1);
        System.out.println(username1);
        System.out.println(username1);
        System.out.println(email);
        System.out.println(email);
        System.out.println(email);
        System.out.println(email);
        System.out.println(email);
        System.out.println(email);
        System.out.println(email);
        System.out.println(email);
        System.out.println(email);
        System.out.println(email);


        String currentUsername = authentication.getName();
        Member member = memberJPARepository.findByUsername(currentUsername);

        if (member == null) {
            // Handle the case where the member is not found, possibly throw an exception
            throw new UsernameNotFoundException("User not found");
        }

        LoginUserDTO userDto = new LoginUserDTO();
        userDto.setUsername(member.getName());
        userDto.setEmail(member.getEmail());
        // Set other fields of userDto as necessary

        return userDto;
    }

    private void validateDuplicateEmail(Member member) {
        List<Member> findEmail = memberJPARepository.findByEmail(member.getEmail());

        if (!findEmail.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 이메일 입니다");
        }
    }

    private void validateDuplicateMember(Member member) {

        List<Member> findMembers = memberRepository.findByName(member.getUsername());
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 이름 입니다");
        }
    }

    public  List<Member> findMembers(){
        return memberRepository.findAll();
    }

    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }

    @Transactional
    public void update(Long id, String name) {
        Member member = memberRepository.findOne(id);
        member.setUsername(name);
    }

    public Member findMemberByUsername(String username) {
        return memberJPARepository.findByUsername(username);

    }
}
