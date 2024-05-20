package garabu.garabuServer.service;

import garabu.garabuServer.domain.Member;
import garabu.garabuServer.repository.MemberJPARepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class CustomUserDetailsServiceTest {

    @Autowired
    private MemberJPARepository memberJPARepository;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Test
    public void testLoadUserByUsername() {
        // Assuming there's a member with this email in the database
        String email = "test@example.com";

        Member member = memberJPARepository.findOneByEmail(email);
        assertNotNull(member, "Member should not be null");

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
        assertNotNull(userDetails, "UserDetails should not be null");
    }
}
