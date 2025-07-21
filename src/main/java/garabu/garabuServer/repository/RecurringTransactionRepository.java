package garabu.garabuServer.repository;

import garabu.garabuServer.domain.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {
    
    List<RecurringTransaction> findByBookId(Long bookId);
    
    List<RecurringTransaction> findByBookIdAndIsActive(Long bookId, Boolean isActive);
    
    @Query("SELECT rt FROM RecurringTransaction rt WHERE rt.book.id = :bookId AND rt.isActive = true")
    List<RecurringTransaction> findActiveByBookId(@Param("bookId") Long bookId);
    
    @Query("SELECT rt FROM RecurringTransaction rt WHERE rt.isActive = true AND rt.nextExecutionDate <= :date")
    List<RecurringTransaction> findDueTransactions(@Param("date") LocalDate date);
    
    @Query("SELECT rt FROM RecurringTransaction rt WHERE rt.book.id = :bookId " +
           "AND rt.isActive = true AND rt.nextExecutionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY rt.nextExecutionDate")
    List<RecurringTransaction> findUpcomingTransactions(@Param("bookId") Long bookId, 
                                                       @Param("startDate") LocalDate startDate, 
                                                       @Param("endDate") LocalDate endDate);
    
    Optional<RecurringTransaction> findByIdAndBookId(Long id, Long bookId);
    
    @Query("SELECT rt FROM RecurringTransaction rt WHERE rt.autoCreate = true " +
           "AND rt.isActive = true AND rt.nextExecutionDate <= :date")
    List<RecurringTransaction> findAutoCreateDueTransactions(@Param("date") LocalDate date);
    
    // 스케줄러를 위한 메서드 추가
    List<RecurringTransaction> findByIsActiveTrueAndNextExecutionDate(LocalDate date);
} 