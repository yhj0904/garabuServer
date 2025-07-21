package garabu.garabuServer.repository;

import garabu.garabuServer.domain.Goal;
import garabu.garabuServer.domain.GoalStatus;
import garabu.garabuServer.domain.GoalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    
    List<Goal> findByBookId(Long bookId);
    
    List<Goal> findByBookIdAndStatus(Long bookId, GoalStatus status);
    
    List<Goal> findByBookIdAndGoalType(Long bookId, GoalType goalType);
    
    List<Goal> findByBookIdAndStatusAndGoalType(Long bookId, GoalStatus status, GoalType goalType);
    
    @Query("SELECT g FROM Goal g WHERE g.book.id = :bookId AND g.status = 'ACTIVE'")
    List<Goal> findActiveGoalsByBookId(@Param("bookId") Long bookId);
    
    @Query("SELECT g FROM Goal g WHERE g.book.id = :bookId AND g.status = 'COMPLETED' ORDER BY g.completedAt DESC")
    List<Goal> findCompletedGoalsByBookId(@Param("bookId") Long bookId);
    
    @Query("SELECT g FROM Goal g WHERE g.status = 'ACTIVE' AND g.targetDate < :date")
    List<Goal> findExpiredGoals(@Param("date") LocalDate date);
    
    @Query("SELECT g FROM Goal g WHERE g.book.id = :bookId AND g.category = :category AND g.status = 'ACTIVE'")
    List<Goal> findActiveGoalsByBookIdAndCategory(@Param("bookId") Long bookId, @Param("category") String category);
    
    Optional<Goal> findByIdAndBookId(Long id, Long bookId);
} 