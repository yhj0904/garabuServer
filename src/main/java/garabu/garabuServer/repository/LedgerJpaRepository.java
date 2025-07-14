package garabu.garabuServer.repository;

import garabu.garabuServer.domain.Ledger;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.AmountType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface LedgerJpaRepository extends JpaRepository<Ledger, Long> {

    List<Ledger> findByMember(Member member);
    
    Page<Ledger> findByBookOrderByDateDesc(Book book, Pageable pageable);
    
     /**
     * 특정 가계부의 모든 기록을 조회 (권한 확인은 서비스 레이어에서 처리)
     */
    @Query("SELECT l FROM Ledger l WHERE l.book = :book ORDER BY l.date DESC, l.id DESC")
    Page<Ledger> findByBookWithAuthorization(@Param("book") Book book, Pageable pageable);
    
    boolean existsByDateAndAmountAndDescriptionAndMemberIdAndBookId(
        LocalDate date, Integer amount, String description, Long memberId, Long bookId);
    
    // 테스트 데이터 관련 메서드
    long countByBook(Book book);
    
    long countByBookAndAmountType(Book book, AmountType amountType);
    
    @Modifying
    @Query("DELETE FROM Ledger l WHERE l.book = :book")
    void deleteAllByBook(@Param("book") Book book);
}
