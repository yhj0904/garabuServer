package garabu.garabuServer.service;

import garabu.garabuServer.domain.*;
import garabu.garabuServer.dto.goal.*;
import garabu.garabuServer.exception.BookAccessException;
import garabu.garabuServer.exception.BookNotFoundException;
import garabu.garabuServer.repository.BookRepository;
import garabu.garabuServer.repository.GoalRepository;
import garabu.garabuServer.repository.UserBookJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoalService {
    
    private final GoalRepository goalRepository;
    private final BookRepository bookRepository;
    private final UserBookJpaRepository userBookRepository;
    private final MemberService memberService;
    
    @Transactional
    public GoalResponse createGoal(Long bookId, CreateGoalRequest request) {
        Member currentMember = memberService.getCurrentMember();
        
        // 가계부 접근 권한 확인
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("가계부를 찾을 수 없습니다."));
        
        checkBookAccess(book, currentMember);
        
        Goal goal = Goal.createGoal(
            book, 
            request.getName(), 
            request.getGoalType(), 
            request.getTargetAmount(), 
            request.getStartDate(), 
            request.getTargetDate()
        );
        
        goal.setDescription(request.getDescription());
        goal.setCategory(request.getCategory());
        goal.setIcon(request.getIcon());
        goal.setColor(request.getColor());
        
        if (request.getCurrentAmount() != null) {
            goal.setCurrentAmount(request.getCurrentAmount());
        }
        
        Goal savedGoal = goalRepository.save(goal);
        return convertToResponse(savedGoal);
    }
    
    public List<GoalResponse> getGoals(Long bookId, GoalType type, Boolean active) {
        Member currentMember = memberService.getCurrentMember();
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("가계부를 찾을 수 없습니다."));
        
        checkBookAccess(book, currentMember);
        
        List<Goal> goals;
        if (type != null && active != null) {
            GoalStatus status = active ? GoalStatus.ACTIVE : GoalStatus.COMPLETED;
            goals = goalRepository.findByBookIdAndStatusAndGoalType(bookId, status, type);
        } else if (type != null) {
            goals = goalRepository.findByBookIdAndGoalType(bookId, type);
        } else if (active != null) {
            GoalStatus status = active ? GoalStatus.ACTIVE : GoalStatus.COMPLETED;
            goals = goalRepository.findByBookIdAndStatus(bookId, status);
        } else {
            goals = goalRepository.findByBookId(bookId);
        }
        
        return goals.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public GoalDetailResponse getGoalDetail(Long goalId) {
        Member currentMember = memberService.getCurrentMember();
        
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("목표를 찾을 수 없습니다."));
        
        checkBookAccess(goal.getBook(), currentMember);
        
        return convertToDetailResponse(goal);
    }
    
    @Transactional
    public GoalResponse updateGoal(Long goalId, UpdateGoalRequest request) {
        Member currentMember = memberService.getCurrentMember();
        
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("목표를 찾을 수 없습니다."));
        
        checkBookAccess(goal.getBook(), currentMember);
        
        if (request.getName() != null) {
            goal.setName(request.getName());
        }
        if (request.getDescription() != null) {
            goal.setDescription(request.getDescription());
        }
        if (request.getTargetAmount() != null) {
            goal.setTargetAmount(request.getTargetAmount());
        }
        if (request.getTargetDate() != null) {
            goal.setTargetDate(request.getTargetDate());
        }
        if (request.getCategory() != null) {
            goal.setCategory(request.getCategory());
        }
        if (request.getIcon() != null) {
            goal.setIcon(request.getIcon());
        }
        if (request.getColor() != null) {
            goal.setColor(request.getColor());
        }
        
        Goal updatedGoal = goalRepository.save(goal);
        return convertToResponse(updatedGoal);
    }
    
    @Transactional
    public GoalProgressResponse updateProgress(Long goalId, UpdateProgressRequest request) {
        Member currentMember = memberService.getCurrentMember();
        
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("목표를 찾을 수 없습니다."));
        
        checkBookAccess(goal.getBook(), currentMember);
        
        if (request.getIsAdditive()) {
            goal.addProgress(request.getAmount());
        } else {
            goal.updateProgress(request.getAmount());
        }
        
        Goal updatedGoal = goalRepository.save(goal);
        
        GoalProgressResponse response = new GoalProgressResponse();
        response.setGoalId(updatedGoal.getId());
        response.setCurrentAmount(updatedGoal.getCurrentAmount());
        response.setTargetAmount(updatedGoal.getTargetAmount());
        response.setProgressPercentage(updatedGoal.getProgressPercentage());
        response.setStatus(updatedGoal.getStatus());
        response.setCompletedAt(updatedGoal.getCompletedAt());
        
        return response;
    }
    
    @Transactional
    public void deleteGoal(Long goalId) {
        Member currentMember = memberService.getCurrentMember();
        
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("목표를 찾을 수 없습니다."));
        
        checkBookAccess(goal.getBook(), currentMember);
        
        goalRepository.delete(goal);
    }
    
    public List<AchievementResponse> getAchievements(Long bookId) {
        Member currentMember = memberService.getCurrentMember();
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("가계부를 찾을 수 없습니다."));
        
        checkBookAccess(book, currentMember);
        
        List<Goal> completedGoals = goalRepository.findCompletedGoalsByBookId(bookId);
        
        return completedGoals.stream()
                .map(this::convertToAchievementResponse)
                .collect(Collectors.toList());
    }
    
    private void checkBookAccess(Book book, Member member) {
        boolean hasAccess = userBookRepository.findByBookIdAndMemberId(book.getId(), member.getId())
                .map(userBook -> !userBook.getBookRole().equals(BookRole.VIEWER))
                .orElse(false);
        
        if (!hasAccess) {
            throw new BookAccessException("이 가계부에 접근할 권한이 없습니다.");
        }
    }
    
    private GoalResponse convertToResponse(Goal goal) {
        GoalResponse response = new GoalResponse();
        response.setId(goal.getId());
        response.setName(goal.getName());
        response.setDescription(goal.getDescription());
        response.setGoalType(goal.getGoalType());
        response.setTargetAmount(goal.getTargetAmount());
        response.setCurrentAmount(goal.getCurrentAmount());
        response.setStartDate(goal.getStartDate());
        response.setTargetDate(goal.getTargetDate());
        response.setStatus(goal.getStatus());
        response.setCategory(goal.getCategory());
        response.setIcon(goal.getIcon());
        response.setColor(goal.getColor());
        response.setProgressPercentage(goal.getProgressPercentage());
        response.setCreatedAt(goal.getCreatedAt());
        response.setUpdatedAt(goal.getUpdatedAt());
        
        return response;
    }
    
    private GoalDetailResponse convertToDetailResponse(Goal goal) {
        GoalDetailResponse response = new GoalDetailResponse();
        response.setId(goal.getId());
        response.setName(goal.getName());
        response.setDescription(goal.getDescription());
        response.setGoalType(goal.getGoalType());
        response.setTargetAmount(goal.getTargetAmount());
        response.setCurrentAmount(goal.getCurrentAmount());
        response.setStartDate(goal.getStartDate());
        response.setTargetDate(goal.getTargetDate());
        response.setStatus(goal.getStatus());
        response.setCategory(goal.getCategory());
        response.setIcon(goal.getIcon());
        response.setColor(goal.getColor());
        response.setProgressPercentage(goal.getProgressPercentage());
        response.setDaysRemaining(calculateDaysRemaining(goal.getTargetDate()));
        response.setIsExpired(goal.isExpired());
        response.setCompletedAt(goal.getCompletedAt());
        response.setCreatedAt(goal.getCreatedAt());
        response.setUpdatedAt(goal.getUpdatedAt());
        
        return response;
    }
    
    private AchievementResponse convertToAchievementResponse(Goal goal) {
        AchievementResponse response = new AchievementResponse();
        response.setId(goal.getId());
        response.setName(goal.getName());
        response.setGoalType(goal.getGoalType());
        response.setTargetAmount(goal.getTargetAmount());
        response.setCompletedAt(goal.getCompletedAt());
        response.setDaysTaken(calculateDaysTaken(goal.getStartDate(), goal.getCompletedAt().toLocalDate()));
        response.setIcon(goal.getIcon());
        response.setColor(goal.getColor());
        
        return response;
    }
    
    private long calculateDaysRemaining(LocalDate targetDate) {
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), targetDate);
    }
    
    private long calculateDaysTaken(LocalDate startDate, LocalDate completedDate) {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, completedDate);
    }
} 