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

/**
 * 회원 관리 서비스 클래스
 * 
 * 회원의 가입, 조회, 수정 등의 비즈니스 로직을 처리합니다.
 * 중복 검증, 인증 정보 관리 등의 기능을 제공합니다.
 * 
 * @author Garabu Team
 * @version 1.0
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService{

    private final MemberRepository memberRepository;
    private final MemberJPARepository memberJPARepository;

    /**
     * 새로운 회원을 등록합니다.
     * 
     * @param member 등록할 회원 정보
     * @return 등록된 회원의 ID
     * @throws IllegalStateException 중복된 사용자명이나 이메일이 있는 경우
     */
    @Transactional
    public Long join (Member member) {
        validateDuplicateMember(member);
        validateDuplicateEmail(member);
        memberRepository.save(member);
        return member.getId();
    }

    /**
     * 현재 로그인한 사용자의 정보를 DTO로 반환합니다.
     * 
     * @return 로그인한 사용자의 정보
     * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우
     */
    public LoginUserDTO getCurrentLoginUserDTO() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomOAuth2User customOAuth2User = (CustomOAuth2User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = customOAuth2User.getName();
        String username1 = customOAuth2User.getUsername();
        String email = customOAuth2User.getEmail();
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

    /**
     * 이메일 중복 검증을 수행합니다.
     * 
     * @param member 검증할 회원 정보
     * @throws IllegalStateException 이미 존재하는 이메일인 경우
     */
    private void validateDuplicateEmail(Member member) {
        List<Member> findEmail = memberJPARepository.findByEmail(member.getEmail());

        if (!findEmail.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 이메일 입니다");
        }
    }

    /**
     * 사용자명 중복 검증을 수행합니다.
     * 
     * @param member 검증할 회원 정보
     * @throws IllegalStateException 이미 존재하는 사용자명인 경우
     */
    private void validateDuplicateMember(Member member) {

        List<Member> findMembers = memberRepository.findByName(member.getUsername());
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 이름 입니다");
        }
    }

    /**
     * 모든 회원 목록을 조회합니다.
     * 
     * @return 전체 회원 목록
     */
    public  List<Member> findMembers(){
        return memberRepository.findAll();
    }

    /**
     * ID로 회원을 조회합니다.
     * 
     * @param memberId 조회할 회원의 ID
     * @return 조회된 회원 정보
     */
    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }

    /**
     * 회원 정보를 수정합니다.
     * 
     * @param id 수정할 회원의 ID
     * @param name 새로운 사용자명
     */
    @Transactional
    public void update(Long id, String name) {
        Member member = memberRepository.findOne(id);
        member.setUsername(name);
    }

    /**
     * 사용자명으로 회원을 조회합니다.
     * 
     * @param username 조회할 사용자명
     * @return 조회된 회원 정보
     */
    public Member findMemberByUsername(String username) {
        return memberJPARepository.findByUsername(username);
    }
}
