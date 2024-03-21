package garabu.garabuServer.repository;

import garabu.garabuServer.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberJPARepository extends JpaRepository<Member, Long> {
    List<Member> findByEmail(String name);

}
