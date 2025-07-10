package garabu.garabuServer.repository;

import garabu.garabuServer.domain.Ledger;
import garabu.garabuServer.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface LedgerJpaRepository extends JpaRepository<Ledger, Long> {

    List<Ledger> findByMember(Member member);
    
    boolean existsByDateAndAmountAndDescriptionAndMemberIdAndBookIdAndCreatedAtAfter(
        LocalDate date, Integer amount, String description, Long memberId, Long bookId, LocalDateTime createdAt);
}
