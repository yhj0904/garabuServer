package garabu.garabuServer.repository;

import garabu.garabuServer.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberJPARepository extends JpaRepository<Member, Long> {
    List<Member> findByEmail(String email);
    Member findByUsername(String username);
    Member findByName(String name);
    Member findOneByEmail(String email);
    Optional<Member> findByProviderIdAndEmail(String providerId, String email);
    
    // 일반로그인 사용자 조회 (providerId가 null인 경우)
    Member findByEmailAndProviderIdIsNull(String email);
    
    // 소셜로그인 사용자 조회 (email + providerId 조합)
    Member findByEmailAndProviderId(String email, String providerId);

}
