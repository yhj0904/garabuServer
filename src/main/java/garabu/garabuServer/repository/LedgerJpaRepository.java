package garabu.garabuServer.repository;

import garabu.garabuServer.domain.Ledger;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.domain.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface LedgerJpaRepository extends JpaRepository<Ledger, Long> {

    List<Ledger> findByMember(Member member);
    
    Page<Ledger> findByBookOrderByDateDesc(Book book, Pageable pageable);
    
    @Query("SELECT l FROM Ledger l WHERE l.book = :book AND l.member IN " +
           "(SELECT ub.member FROM UserBook ub WHERE ub.book = :book)")
    Page<Ledger> findByBookWithAuthorization(@Param("book") Book book, Pageable pageable);
    
    boolean existsByDateAndAmountAndDescriptionAndMemberIdAndBookId(
        LocalDate date, Integer amount, String description, Long memberId, Long bookId);
}
