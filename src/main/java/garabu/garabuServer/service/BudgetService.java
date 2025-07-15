package garabu.garabuServer.service;

import garabu.garabuServer.domain.AmountType;
import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.Budget;
import garabu.garabuServer.domain.Ledger;
import garabu.garabuServer.dto.BudgetRequest;
import garabu.garabuServer.dto.BudgetResponse;
import garabu.garabuServer.dto.BudgetSummaryResponse;
import garabu.garabuServer.repository.BudgetJpaRepository;
import garabu.garabuServer.repository.LedgerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetService {

    private final BudgetJpaRepository budgetJpaRepository;
    private final LedgerJpaRepository ledgerJpaRepository;
    private final BookService bookService;

    /**
     * 예산 생성
     */
    @Transactional
    public BudgetResponse createBudget(Long bookId, BudgetRequest request) {
        Book book = bookService.findById(bookId);
        
        // 이미 해당 년월의 예산이 있는지 확인
        if (budgetJpaRepository.existsByBookAndBudgetMonth(book, request.getBudgetMonth())) {
            throw new RuntimeException("해당 년월의 예산이 이미 존재합니다.");
        }

        Budget budget = new Budget();
        budget.setBook(book);
        budget.setBudgetMonth(request.getBudgetMonth());
        budget.setIncomeBudget(request.getIncomeBudget());
        budget.setExpenseBudget(request.getExpenseBudget());
        budget.setMemo(request.getMemo());

        Budget savedBudget = budgetJpaRepository.save(budget);
        return BudgetResponse.from(savedBudget);
    }

    /**
     * 예산 수정
     */
    @Transactional
    public BudgetResponse updateBudget(Long bookId, String budgetMonth, BudgetRequest request) {
        Book book = bookService.findById(bookId);
        
        Budget budget = budgetJpaRepository.findByBookAndBudgetMonth(book, budgetMonth)
                .orElseThrow(() -> new RuntimeException("해당 예산을 찾을 수 없습니다."));

        budget.setIncomeBudget(request.getIncomeBudget());
        budget.setExpenseBudget(request.getExpenseBudget());
        budget.setMemo(request.getMemo());

        Budget updatedBudget = budgetJpaRepository.save(budget);
        return BudgetResponse.from(updatedBudget);
    }

    /**
     * 예산 삭제
     */
    @Transactional
    public void deleteBudget(Long bookId, String budgetMonth) {
        Book book = bookService.findById(bookId);
        
        Budget budget = budgetJpaRepository.findByBookAndBudgetMonth(book, budgetMonth)
                .orElseThrow(() -> new RuntimeException("해당 예산을 찾을 수 없습니다."));

        budgetJpaRepository.delete(budget);
    }

    /**
     * 가계부별 예산 목록 조회
     */
    public List<BudgetResponse> getBudgetsByBook(Long bookId) {
        Book book = bookService.findById(bookId);
        List<Budget> budgets = budgetJpaRepository.findByBookOrderByBudgetMonthDesc(book);
        
        return budgets.stream()
                .map(BudgetResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 년월 예산 조회
     */
    public BudgetResponse getBudgetByMonth(Long bookId, String budgetMonth) {
        Book book = bookService.findById(bookId);
        
        // 예산이 없으면 null 반환
        Optional<Budget> optionalBudget = budgetJpaRepository.findByBookAndBudgetMonth(book, budgetMonth);
        if (optionalBudget.isEmpty()) {
            return null;
        }

        return BudgetResponse.from(optionalBudget.get());
    }

    /**
     * 예산 요약 정보 조회 (실제 수입/지출과 비교)
     */
    public BudgetSummaryResponse getBudgetSummary(Long bookId, String budgetMonth) {
        Book book = bookService.findById(bookId);
        
        // 예산이 없으면 null 반환
        Optional<Budget> optionalBudget = budgetJpaRepository.findByBookAndBudgetMonth(book, budgetMonth);
        if (optionalBudget.isEmpty()) {
            return null;
        }
        
        Budget budget = optionalBudget.get();

        // 해당 월의 실제 수입/지출 계산
        int actualIncome = calculateActualAmount(book, budgetMonth, AmountType.INCOME);
        int actualExpense = calculateActualAmount(book, budgetMonth, AmountType.EXPENSE);

        // 달성률 계산
        double incomeAchievementRate = budget.getIncomeBudget() != null && budget.getIncomeBudget() > 0 
                ? (double) actualIncome / budget.getIncomeBudget() * 100 : 0;
        double expenseAchievementRate = budget.getExpenseBudget() != null && budget.getExpenseBudget() > 0 
                ? (double) actualExpense / budget.getExpenseBudget() * 100 : 0;

        // 차이 계산
        int incomeDifference = budget.getIncomeBudget() != null ? actualIncome - budget.getIncomeBudget() : 0;
        int expenseDifference = budget.getExpenseBudget() != null ? actualExpense - budget.getExpenseBudget() : 0;

        BudgetSummaryResponse response = new BudgetSummaryResponse();
        response.setId(budget.getId());
        response.setBookId(budget.getBook().getId());
        response.setBudgetMonth(budget.getBudgetMonth());
        response.setIncomeBudget(budget.getIncomeBudget());
        response.setExpenseBudget(budget.getExpenseBudget());
        response.setActualIncome(actualIncome);
        response.setActualExpense(actualExpense);
        response.setIncomeAchievementRate(Math.round(incomeAchievementRate * 10.0) / 10.0);
        response.setExpenseAchievementRate(Math.round(expenseAchievementRate * 10.0) / 10.0);
        response.setIncomeDifference(incomeDifference);
        response.setExpenseDifference(expenseDifference);
        response.setMemo(budget.getMemo());
        response.setCreatedAt(budget.getCreatedAt().toString());
        response.setUpdatedAt(budget.getUpdatedAt().toString());

        return response;
    }

    /**
     * 특정 년도의 예산 목록 조회
     */
    public List<BudgetResponse> getBudgetsByYear(Long bookId, String year) {
        Book book = bookService.findById(bookId);
        List<Budget> budgets = budgetJpaRepository.findByBookAndYear(book, year);
        
        return budgets.stream()
                .map(BudgetResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 최근 N개월 예산 조회
     */
    public List<BudgetResponse> getRecentBudgets(Long bookId, int limit) {
        Book book = bookService.findById(bookId);
        List<Budget> budgets = budgetJpaRepository.findRecentBudgets(book, limit);
        
        return budgets.stream()
                .map(BudgetResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 해당 월의 실제 수입/지출 계산
     */
    private int calculateActualAmount(Book book, String budgetMonth, AmountType amountType) {
        // budgetMonth를 파싱하여 시작일과 종료일 계산
        String[] parts = budgetMonth.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        // 해당 기간의 수입/지출 합계 계산
        List<Ledger> ledgers = ledgerJpaRepository.findByBook(book);
        
        return ledgers.stream()
                .filter(ledger -> ledger.getAmountType() == amountType)
                .filter(ledger -> !ledger.getDate().isBefore(startDate) && !ledger.getDate().isAfter(endDate))
                .mapToInt(Ledger::getAmount)
                .sum();
    }
} 