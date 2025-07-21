package garabu.garabuServer.repository;

import garabu.garabuServer.domain.BudgetAlert;
import garabu.garabuServer.domain.Budget;
import garabu.garabuServer.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetAlertRepository extends JpaRepository<BudgetAlert, Long> {
    
    List<BudgetAlert> findByMemberId(Long memberId);
    
    List<BudgetAlert> findByBudgetId(Long budgetId);
    
    List<BudgetAlert> findByMemberIdAndIsActiveTrue(Long memberId);
    
    Optional<BudgetAlert> findByBudgetIdAndMemberId(Long budgetId, Long memberId);
    
    @Query("SELECT ba FROM BudgetAlert ba WHERE ba.budget.book.id = :bookId AND ba.member.id = :memberId")
    List<BudgetAlert> findByBookIdAndMemberId(@Param("bookId") Long bookId, @Param("memberId") Long memberId);
    
    @Query("SELECT ba FROM BudgetAlert ba WHERE ba.isActive = true AND ba.budget.budgetMonth = :budgetMonth")
    List<BudgetAlert> findActiveAlertsByMonth(@Param("budgetMonth") String budgetMonth);
    
    List<BudgetAlert> findByMemberOrderByCreatedAtDesc(Member member);
    
    boolean existsByMemberAndBudget(Member member, Budget budget);
} 