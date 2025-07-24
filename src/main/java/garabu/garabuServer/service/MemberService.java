package garabu.garabuServer.service;

import garabu.garabuServer.domain.Member;
import garabu.garabuServer.domain.NotificationPreference;
import garabu.garabuServer.dto.CustomOAuth2User;
import garabu.garabuServer.dto.LoginUserDTO;
import garabu.garabuServer.jwt.CustomUserDetails;
import garabu.garabuServer.repository.MemberJPARepository;
import garabu.garabuServer.repository.MemberRepository;
import garabu.garabuServer.repository.NotificationPreferenceRepository;
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
import garabu.garabuServer.domain.SystemRole;

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
    private final NotificationPreferenceRepository notificationPreferenceRepository;

    /**
     * 새로운 회원을 등록합니다.
     * 
     * @param member 등록할 회원 정보
     * @return 등록된 회원의 ID
     * @throws IllegalStateException 중복된 사용자명이나 이메일이 있는 경우
     */
    @Transactional
    public Long join (Member member) {
        // username 중복 체크 (name은 동명이인 허용)
        validateDuplicateUsername(member);
        validateDuplicateEmail(member);
        
        // 기본 권한 설정 (회원가입 시 기본적으로 USER 권한 부여)
        if (member.getSystemRole() == null) {
            member.setSystemRole(SystemRole.ROLE_USER);
        }
        
        memberRepository.save(member);
        
        // 회원가입 시 기본 알림 설정 생성
        NotificationPreference notificationPreference = new NotificationPreference(member);
        notificationPreferenceRepository.save(notificationPreference);
        
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
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("인증되지 않은 사용자입니다.");
        }
        
        String username = authentication.getName();
        Member member = memberJPARepository.findByUsername(username);

        if (member == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
        }

        LoginUserDTO userDto = new LoginUserDTO();
        userDto.setId(member.getId());
        userDto.setUsername(member.getUsername());
        userDto.setName(member.getName());
        userDto.setEmail(member.getEmail());
        userDto.setRole(member.getRole()); // getRole()은 SystemRole을 String으로 변환
        
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
     * 사용자명(username) 중복 검증을 수행합니다.
     * 실제 이름(name)은 동명이인을 허용하므로 중복 체크하지 않습니다.
     * 
     * @param member 검증할 회원 정보
     * @throws IllegalStateException 이미 존재하는 사용자명인 경우
     */
    private void validateDuplicateUsername(Member member) {
        Member findMember = memberJPARepository.findByUsername(member.getUsername());
        if (findMember != null) {
            throw new IllegalStateException("이미 존재하는 사용자명(ID) 입니다");
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

    /**
     * 현재 로그인한 사용자의 Member 엔티티를 반환합니다.
     * 
     * @return 로그인한 사용자의 Member 엔티티
     * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우
     */
    public Member getCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("인증되지 않은 사용자입니다.");
        }
        
        String username = authentication.getName();
        Member member = memberJPARepository.findByUsername(username);

        if (member == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
        }

        return member;
    }

    /**
     * ID로 회원을 조회합니다. (JPA 방식)
     * 
     * @param memberId 조회할 회원의 ID
     * @return 조회된 회원 정보
     * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우
     */
    public Member findById(Long memberId) {
        return memberJPARepository.findById(memberId)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + memberId));
    }
}
