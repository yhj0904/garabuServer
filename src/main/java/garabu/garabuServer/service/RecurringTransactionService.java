package garabu.garabuServer.service;

import garabu.garabuServer.domain.*;
import garabu.garabuServer.dto.recurring.*;
import garabu.garabuServer.exception.BookAccessException;
import garabu.garabuServer.exception.BookNotFoundException;
import garabu.garabuServer.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
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
public class RecurringTransactionService {
    
    private final RecurringTransactionRepository recurringTransactionRepository;
    private final BookRepository bookRepository;
    private final UserBookJpaRepository userBookRepository;
    private final LedgerJpaRepository ledgerRepository;
    private final CategoryJpaRepository categoryRepository;
    private final PaymentJpaRepository paymentRepository;
    private final AssetJpaRepository assetRepository;
    private final MemberService memberService;
    
    @Transactional
    public RecurringTransactionResponse create(Long bookId, CreateRecurringTransactionRequest request) {
        Member currentMember = memberService.getCurrentMember();
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("가계부를 찾을 수 없습니다."));
        
        checkBookAccess(book, currentMember);
        
        RecurringTransaction transaction = RecurringTransaction.create(
            book,
            request.getName(),
            request.getAmountType(),
            request.getAmount(),
            request.getRecurrenceType(),
            request.getStartDate()
        );
        
        transaction.setDescription(request.getDescription());
        
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
            transaction.setCategory(category);
        }
        
        if (request.getPaymentMethodId() != null) {
            PaymentMethod paymentMethod = paymentRepository.findById(request.getPaymentMethodId())
                    .orElseThrow(() -> new IllegalArgumentException("결제 수단을 찾을 수 없습니다."));
            transaction.setPaymentMethod(paymentMethod);
        }
        
        if (request.getAssetId() != null) {
            Asset asset = assetRepository.findById(request.getAssetId())
                    .orElseThrow(() -> new IllegalArgumentException("자산을 찾을 수 없습니다."));
            transaction.setAsset(asset);
        }
        
        transaction.setRecurrenceInterval(request.getRecurrenceInterval());
        transaction.setRecurrenceDay(request.getRecurrenceDay());
        transaction.setEndDate(request.getEndDate());
        transaction.setMaxExecutions(request.getMaxExecutions());
        transaction.setAutoCreate(request.getAutoCreate() != null ? request.getAutoCreate() : true);
        
        RecurringTransaction saved = recurringTransactionRepository.save(transaction);
        return convertToResponse(saved);
    }
    
    public List<RecurringTransactionResponse> getByBookId(Long bookId, Boolean active) {
        Member currentMember = memberService.getCurrentMember();
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("가계부를 찾을 수 없습니다."));
        
        checkBookAccess(book, currentMember);
        
        List<RecurringTransaction> transactions;
        if (active != null) {
            transactions = recurringTransactionRepository.findByBookIdAndIsActive(bookId, active);
        } else {
            transactions = recurringTransactionRepository.findByBookId(bookId);
        }
        
        return transactions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public RecurringTransactionDetailResponse getDetail(Long recurringId) {
        Member currentMember = memberService.getCurrentMember();
        
        RecurringTransaction transaction = recurringTransactionRepository.findById(recurringId)
                .orElseThrow(() -> new IllegalArgumentException("반복 거래를 찾을 수 없습니다."));
        
        checkBookAccess(transaction.getBook(), currentMember);
        
        return convertToDetailResponse(transaction);
    }
    
    @Transactional
    public RecurringTransactionResponse update(Long recurringId, UpdateRecurringTransactionRequest request) {
        Member currentMember = memberService.getCurrentMember();
        
        RecurringTransaction transaction = recurringTransactionRepository.findById(recurringId)
                .orElseThrow(() -> new IllegalArgumentException("반복 거래를 찾을 수 없습니다."));
        
        checkBookAccess(transaction.getBook(), currentMember);
        
        if (request.getName() != null) {
            transaction.setName(request.getName());
        }
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }
        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getRecurrenceInterval() != null) {
            transaction.setRecurrenceInterval(request.getRecurrenceInterval());
        }
        if (request.getEndDate() != null) {
            transaction.setEndDate(request.getEndDate());
        }
        if (request.getAutoCreate() != null) {
            transaction.setAutoCreate(request.getAutoCreate());
        }
        
        RecurringTransaction updated = recurringTransactionRepository.save(transaction);
        return convertToResponse(updated);
    }
    
    @Transactional
    public RecurringTransactionResponse pause(Long recurringId) {
        Member currentMember = memberService.getCurrentMember();
        
        RecurringTransaction transaction = recurringTransactionRepository.findById(recurringId)
                .orElseThrow(() -> new IllegalArgumentException("반복 거래를 찾을 수 없습니다."));
        
        checkBookAccess(transaction.getBook(), currentMember);
        
        transaction.pause();
        RecurringTransaction updated = recurringTransactionRepository.save(transaction);
        return convertToResponse(updated);
    }
    
    @Transactional
    public RecurringTransactionResponse resume(Long recurringId) {
        Member currentMember = memberService.getCurrentMember();
        
        RecurringTransaction transaction = recurringTransactionRepository.findById(recurringId)
                .orElseThrow(() -> new IllegalArgumentException("반복 거래를 찾을 수 없습니다."));
        
        checkBookAccess(transaction.getBook(), currentMember);
        
        transaction.resume();
        RecurringTransaction updated = recurringTransactionRepository.save(transaction);
        return convertToResponse(updated);
    }
    
    @Transactional
    public void delete(Long recurringId) {
        Member currentMember = memberService.getCurrentMember();
        
        RecurringTransaction transaction = recurringTransactionRepository.findById(recurringId)
                .orElseThrow(() -> new IllegalArgumentException("반복 거래를 찾을 수 없습니다."));
        
        checkBookAccess(transaction.getBook(), currentMember);
        
        recurringTransactionRepository.delete(transaction);
    }
    
    @Transactional
    public RecurringExecutionResponse executeManually(Long recurringId, LocalDate executionDate) {
        Member currentMember = memberService.getCurrentMember();
        
        RecurringTransaction transaction = recurringTransactionRepository.findById(recurringId)
                .orElseThrow(() -> new IllegalArgumentException("반복 거래를 찾을 수 없습니다."));
        
        checkBookAccess(transaction.getBook(), currentMember);
        
        Ledger ledger = createLedgerFromRecurring(transaction, executionDate, currentMember);
        
        RecurringExecutionResponse response = new RecurringExecutionResponse();
        response.setRecurringTransactionId(recurringId);
        response.setLedgerId(ledger.getId());
        response.setExecutionDate(executionDate);
        response.setAmount(transaction.getAmount());
        response.setMessage("반복 거래가 실행되었습니다.");
        
        return response;
    }
    
    public List<UpcomingTransactionResponse> getUpcoming(Long bookId, int days) {
        Member currentMember = memberService.getCurrentMember();
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("가계부를 찾을 수 없습니다."));
        
        checkBookAccess(book, currentMember);
        
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);
        
        List<RecurringTransaction> upcoming = recurringTransactionRepository
                .findUpcomingTransactions(bookId, startDate, endDate);
        
        return upcoming.stream()
                .map(this::convertToUpcomingResponse)
                .collect(Collectors.toList());
    }
    
    // 매일 자정에 실행되는 스케줄러
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void executeScheduledTransactions() {
        log.info("반복 거래 자동 실행 시작");
        
        LocalDate today = LocalDate.now();
        List<RecurringTransaction> dueTransactions = recurringTransactionRepository
                .findAutoCreateDueTransactions(today);
        
        for (RecurringTransaction transaction : dueTransactions) {
            try {
                // 가계부 소유자를 실행자로 설정
                Member owner = transaction.getBook().getOwner();
                Ledger ledger = createLedgerFromRecurring(transaction, today, owner);
                
                transaction.execute();
                recurringTransactionRepository.save(transaction);
                
                log.info("반복 거래 자동 실행 완료: {} -> Ledger ID: {}", 
                        transaction.getName(), ledger.getId());
            } catch (Exception e) {
                log.error("반복 거래 자동 실행 실패: {}", transaction.getName(), e);
            }
        }
        
        log.info("반복 거래 자동 실행 종료");
    }
    
    private Ledger createLedgerFromRecurring(RecurringTransaction transaction, LocalDate date, Member member) {
        Ledger ledger = new Ledger();
        ledger.setMember(member);
        ledger.setBook(transaction.getBook());
        ledger.setCategory(transaction.getCategory());
        ledger.setPaymentMethod(transaction.getPaymentMethod());
        ledger.setAmountType(transaction.getAmountType());
        ledger.setDate(date);
        ledger.setAmount(transaction.getAmount().longValue());
        ledger.setDescription(transaction.getName());
        ledger.setMemo("반복 거래: " + transaction.getDescription());
        ledger.setSpender(member.getUsername());
        
        return ledgerRepository.save(ledger);
    }
    
    private void checkBookAccess(Book book, Member member) {
        boolean hasAccess = userBookRepository.findByBookIdAndMemberId(book.getId(), member.getId())
                .map(userBook -> !userBook.getBookRole().equals(BookRole.VIEWER))
                .orElse(false);
        
        if (!hasAccess) {
            throw new BookAccessException("이 가계부에 접근할 권한이 없습니다.");
        }
    }
    
    private RecurringTransactionResponse convertToResponse(RecurringTransaction transaction) {
        RecurringTransactionResponse response = new RecurringTransactionResponse();
        response.setId(transaction.getId());
        response.setName(transaction.getName());
        response.setDescription(transaction.getDescription());
        response.setAmountType(transaction.getAmountType());
        response.setAmount(transaction.getAmount());
        response.setRecurrenceType(transaction.getRecurrenceType());
        response.setRecurrenceInterval(transaction.getRecurrenceInterval());
        response.setStartDate(transaction.getStartDate());
        response.setEndDate(transaction.getEndDate());
        response.setNextExecutionDate(transaction.getNextExecutionDate());
        response.setIsActive(transaction.getIsActive());
        response.setExecutionCount(transaction.getExecutionCount());
        response.setCreatedAt(transaction.getCreatedAt());
        
        if (transaction.getCategory() != null) {
            response.setCategoryId(transaction.getCategory().getId());
            response.setCategoryName(transaction.getCategory().getCategory());
        }
        
        return response;
    }
    
    private RecurringTransactionDetailResponse convertToDetailResponse(RecurringTransaction transaction) {
        RecurringTransactionDetailResponse response = new RecurringTransactionDetailResponse();
        response.setId(transaction.getId());
        response.setName(transaction.getName());
        response.setDescription(transaction.getDescription());
        response.setAmountType(transaction.getAmountType());
        response.setAmount(transaction.getAmount());
        response.setRecurrenceType(transaction.getRecurrenceType());
        response.setRecurrenceInterval(transaction.getRecurrenceInterval());
        response.setRecurrenceDay(transaction.getRecurrenceDay());
        response.setStartDate(transaction.getStartDate());
        response.setEndDate(transaction.getEndDate());
        response.setNextExecutionDate(transaction.getNextExecutionDate());
        response.setLastExecutionDate(transaction.getLastExecutionDate());
        response.setIsActive(transaction.getIsActive());
        response.setExecutionCount(transaction.getExecutionCount());
        response.setMaxExecutions(transaction.getMaxExecutions());
        response.setAutoCreate(transaction.getAutoCreate());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setUpdatedAt(transaction.getUpdatedAt());
        
        if (transaction.getCategory() != null) {
            response.setCategoryId(transaction.getCategory().getId());
            response.setCategoryName(transaction.getCategory().getCategory());
        }
        
        if (transaction.getPaymentMethod() != null) {
            response.setPaymentMethodId(transaction.getPaymentMethod().getId());
            response.setPaymentMethodName(transaction.getPaymentMethod().getPayment());
        }
        
        return response;
    }
    
    private UpcomingTransactionResponse convertToUpcomingResponse(RecurringTransaction transaction) {
        UpcomingTransactionResponse response = new UpcomingTransactionResponse();
        response.setId(transaction.getId());
        response.setName(transaction.getName());
        response.setAmountType(transaction.getAmountType());
        response.setAmount(transaction.getAmount());
        response.setExecutionDate(transaction.getNextExecutionDate());
        
        if (transaction.getCategory() != null) {
            response.setCategoryName(transaction.getCategory().getCategory());
        }
        
        return response;
    }
} 