package garabu.garabuServer.repository;

import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.Receipt;
import garabu.garabuServer.domain.ReceiptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    
    List<Receipt> findByBookId(Long bookId);
    
    List<Receipt> findByBookIdAndStatus(Long bookId, ReceiptStatus status);
    
    List<Receipt> findByUploadedById(Long memberId);
    
    @Query("SELECT r FROM Receipt r WHERE r.book.id = :bookId ORDER BY r.createdAt DESC")
    List<Receipt> findByBookIdOrderByCreatedAtDesc(@Param("bookId") Long bookId);
    
    @Query("SELECT r FROM Receipt r JOIN r.linkedLedgers l WHERE l.id = :ledgerId")
    List<Receipt> findByLedgerId(@Param("ledgerId") Long ledgerId);
    
    @Query("SELECT r FROM Receipt r WHERE r.book.id = :bookId AND r.purchaseDate BETWEEN :startDate AND :endDate")
    List<Receipt> findByBookIdAndDateRange(@Param("bookId") Long bookId, 
                                          @Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);
    
    Optional<Receipt> findByIdAndBookId(Long id, Long bookId);
    
    @Query("SELECT r FROM Receipt r WHERE r.status = :status AND r.createdAt < :date")
    List<Receipt> findOldReceiptsByStatus(@Param("status") ReceiptStatus status, 
                                          @Param("date") LocalDate date);
    
    List<Receipt> findByBookOrderByCreatedAtDesc(Book book);
} 