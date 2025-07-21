package garabu.garabuServer.repository;

import garabu.garabuServer.domain.NotificationPreference;
import garabu.garabuServer.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
    
    Optional<NotificationPreference> findByMemberId(Long memberId);
    
    Optional<NotificationPreference> findByMember(Member member);
    
    boolean existsByMemberId(Long memberId);
} 