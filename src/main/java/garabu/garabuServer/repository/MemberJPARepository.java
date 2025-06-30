package garabu.garabuServer.repository;

import garabu.garabuServer.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberJPARepository extends JpaRepository<Member, Long> {
    List<Member> findByEmail(String email);
    Member findByUsername(String username);
    Member findByName(String name);
    Member findOneByEmail(String email);
    Optional<Member> findByProviderIdAndEmail(String providerId, String email);

}
