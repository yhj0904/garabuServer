package garabu.garabuServer.repository;

import garabu.garabuServer.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberJPARepository extends JpaRepository<Member, Long> {
    List<Member> findByEmail(String email);
    Member findByUsername(String username);
    Member findByName(String name);
    Member findOneByEmail(String email);
}
