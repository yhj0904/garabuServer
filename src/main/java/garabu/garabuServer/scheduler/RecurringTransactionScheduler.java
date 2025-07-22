package garabu.garabuServer.scheduler;

import garabu.garabuServer.domain.RecurringTransaction;
import garabu.garabuServer.domain.AmountType;
import garabu.garabuServer.domain.Ledger;
import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.domain.Category;
import garabu.garabuServer.domain.PaymentMethod;
import garabu.garabuServer.repository.RecurringTransactionRepository;
import garabu.garabuServer.service.NotificationService;
import garabu.garabuServer.service.LedgerService;
import garabu.garabuServer.service.BookService;
import garabu.garabuServer.service.MemberService;
import garabu.garabuServer.service.CategoryService;
import garabu.garabuServer.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecurringTransactionScheduler {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final LedgerService ledgerService;
    private final NotificationService notificationService;
    private final BookService bookService;
    private final MemberService memberService;
    private final CategoryService categoryService;
    private final PaymentService paymentService;

    // 매일 오전 9시에 실행
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void executeRecurringTransactions() {
        log.info("반복거래 자동 실행 시작");
        
        LocalDate today = LocalDate.now();
        List<RecurringTransaction> transactionsToExecute = recurringTransactionRepository
                .findByIsActiveTrueAndNextExecutionDate(today);
        
        log.info("오늘 실행할 반복거래 {}개 발견", transactionsToExecute.size());
        
        for (RecurringTransaction transaction : transactionsToExecute) {
            try {
                executeTransaction(transaction);
            } catch (Exception e) {
                log.error("반복거래 실행 실패: transactionId={}, error={}", 
                    transaction.getId(), e.getMessage());
            }
        }
    }

    private void executeTransaction(RecurringTransaction transaction) {
        try {
            // 필요한 엔티티 조회
            Book book = transaction.getBook();
            Member member = book.getOwner(); // 가계부 소유자를 거래 생성자로 사용
            
            // 기본 카테고리 조회 (수입 또는 지출)
            String categoryName = transaction.getAmountType() == AmountType.INCOME ? "급여" : "기타";
            Category category = categoryService.findByBookAndCategory(book, categoryName);
            if (category == null) {
                // 카테고리가 없으면 첫 번째 카테고리 사용
                category = categoryService.findByBook(book).stream().findFirst()
                        .orElseThrow(() -> new RuntimeException("가계부에 카테고리가 없습니다"));
            }
            
            // 기본 결제수단 조회
            PaymentMethod paymentMethod = paymentService.findByBookAndPayment(book, "현금");
            if (paymentMethod == null) {
                // 결제수단이 없으면 첫 번째 결제수단 사용
                paymentMethod = paymentService.findByBook(book).stream().findFirst()
                        .orElseThrow(() -> new RuntimeException("가계부에 결제수단이 없습니다"));
            }
            
            // Ledger 엔티티 생성
            Ledger ledger = new Ledger();
            ledger.setDate(LocalDate.now());
            ledger.setAmount(transaction.getAmount().intValue());
            ledger.setDescription(transaction.getName());
            ledger.setMemo("반복거래 자동 실행");
            ledger.setAmountType(transaction.getAmountType());
            ledger.setMember(member);
            ledger.setBook(book);
            ledger.setCategory(category);
            ledger.setPaymentMethod(paymentMethod);
            ledger.setSpender(member.getName());
            
            // 거래 저장
            Long ledgerId = ledgerService.registLedger(ledger);
            
            // 마지막 실행일 업데이트
            transaction.setLastExecutionDate(LocalDate.now());
            
            // 다음 실행일 계산
            LocalDate nextDate = calculateNextExecutionDate(transaction);
            transaction.setNextExecutionDate(nextDate);
            
            recurringTransactionRepository.save(transaction);
            
            // 알림 전송
            notificationService.sendRecurringTransactionNotification(
                member.getId(),
                transaction.getName(),
                transaction.getAmount().intValue()
            );
            
            log.info("반복거래 실행 완료: transactionId={}, ledgerId={}", 
                transaction.getId(), ledgerId);
            
        } catch (Exception e) {
            log.error("반복거래 실행 중 오류 발생: transactionId={}, error={}", 
                transaction.getId(), e.getMessage(), e);
            throw e;
        }
    }

    private LocalDate calculateNextExecutionDate(RecurringTransaction transaction) {
        LocalDate baseDate = LocalDate.now();
        
        switch (transaction.getRecurrenceType()) {
            case DAILY:
                return baseDate.plusDays(1);
            case WEEKLY:
                return baseDate.plusWeeks(1);
            case MONTHLY:
                return baseDate.plusMonths(1);
            case YEARLY:
                return baseDate.plusYears(1);
            default:
                throw new IllegalArgumentException("Unknown recurrence type: " + 
                    transaction.getRecurrenceType());
        }
    }

    // 매일 오전 8시에 알림 전송 (실행 1시간 전)
    @Scheduled(cron = "0 0 8 * * *")
    public void sendRecurringTransactionReminders() {
        log.info("반복거래 알림 전송 시작");
        
        LocalDate today = LocalDate.now();
        List<RecurringTransaction> transactionsToday = recurringTransactionRepository
                .findByIsActiveTrueAndNextExecutionDate(today);
        
        for (RecurringTransaction transaction : transactionsToday) {
            try {
                Member member = transaction.getBook().getOwner();
                notificationService.sendRecurringTransactionReminder(
                    member.getId(),
                    transaction.getName(),
                    transaction.getAmount().intValue()
                );
            } catch (Exception e) {
                log.error("반복거래 알림 전송 실패: transactionId={}, error={}", 
                    transaction.getId(), e.getMessage());
            }
        }
        
        log.info("반복거래 알림 전송 완료: {}개", transactionsToday.size());
    }
}