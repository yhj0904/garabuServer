package garabu.garabuServer.service;

import garabu.garabuServer.domain.Member;
import garabu.garabuServer.jwt.CustomUserDetails;
import garabu.garabuServer.repository.MemberJPARepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberJPARepository memberJPARepository;

    public CustomUserDetailsService(MemberJPARepository memberJPARepository) {

        this.memberJPARepository = memberJPARepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        //DB에서 조회
        Member member = memberJPARepository.findByUsername(username);

        if (member == null) {
            // 사용자를 찾을 수 없을 때 UsernameNotFoundException 던지기
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        // UserDetails에 담아서 반환하면 AuthenticationManager가 검증함
        return new CustomUserDetails(member);
    }
}