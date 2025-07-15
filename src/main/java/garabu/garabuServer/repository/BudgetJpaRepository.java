package garabu.garabuServer.repository;

import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BudgetJpaRepository extends JpaRepository<Budget, Long> {

    /**
     * 가계부별 예산 목록 조회
     */
    List<Budget> findByBookOrderByBudgetMonthDesc(Book book);

    /**
     * 가계부와 년월로 예산 조회
     */
    Optional<Budget> findByBookAndBudgetMonth(Book book, String budgetMonth);

    /**
     * 가계부와 년월로 예산 존재 여부 확인
     */
    boolean existsByBookAndBudgetMonth(Book book, String budgetMonth);

    /**
     * 특정 년도의 예산 목록 조회
     */
    @Query("SELECT b FROM Budget b WHERE b.book = :book AND b.budgetMonth LIKE :year% ORDER BY b.budgetMonth DESC")
    List<Budget> findByBookAndYear(@Param("book") Book book, @Param("year") String year);

    /**
     * 최근 N개월 예산 조회
     */
    @Query("SELECT b FROM Budget b WHERE b.book = :book ORDER BY b.budgetMonth DESC LIMIT :limit")
    List<Budget> findRecentBudgets(@Param("book") Book book, @Param("limit") int limit);
} 