package garabu.garabuServer.repository;

import garabu.garabuServer.domain.Member;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

    private final EntityManager em;

    public void save(Member member) {em.persist(member);}

    public Member findOne(Long id) {return em.find(Member.class, id);}

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }
    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", name)
                .getResultList();
    }
    
    public java.util.Optional<Member> findByEmail(String email) {
        List<Member> results = em.createQuery("select m from Member m where m.email = :email", Member.class)
                .setParameter("email", email)
                .getResultList();
        return results.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(results.get(0));
    }
    
    public Member findByUsername(String username) {
        List<Member> results = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
}
