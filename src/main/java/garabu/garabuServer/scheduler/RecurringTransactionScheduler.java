package garabu.garabuServer.scheduler;

import garabu.garabuServer.domain.RecurringTransaction;
import garabu.garabuServer.domain.Ledger;
import garabu.garabuServer.domain.AmountType;
import garabu.garabuServer.repository.RecurringTransactionRepository;
import garabu.garabuServer.repository.LedgerRepository;
import garabu.garabuServer.service.NotificationService;
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
    private final LedgerRepository ledgerRepository;
    private final NotificationService notificationService;

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
        // 거래 생성
        Ledger ledger = Ledger.builder()
                .date(LocalDate.now())
                .amount(transaction.getAmount())
                .description(transaction.getName())
                .memo("반복거래 자동 실행")
                .amountType(transaction.getAmountType())
                .bookId(transaction.getBookId())
                .payment("자동")
                .category(transaction.getAmountType() == AmountType.INCOME ? "수입" : "지출")
                .spender("시스템")
                .memberId(transaction.getMemberId())
                .build();
        
        ledgerRepository.save(ledger);
        
        // 마지막 실행일 업데이트
        transaction.setLastExecutedDate(LocalDate.now());
        
        // 다음 실행일 계산
        LocalDate nextDate = calculateNextExecutionDate(transaction);
        transaction.setNextExecutionDate(nextDate);
        
        recurringTransactionRepository.save(transaction);
        
        // 알림 전송
        notificationService.sendRecurringTransactionNotification(
            transaction.getMemberId(),
            transaction.getName(),
            transaction.getAmount()
        );
        
        log.info("반복거래 실행 완료: transactionId={}, ledgerId={}", 
            transaction.getId(), ledger.getId());
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
                notificationService.sendRecurringTransactionReminder(
                    transaction.getMemberId(),
                    transaction.getName(),
                    transaction.getAmount()
                );
            } catch (Exception e) {
                log.error("반복거래 알림 전송 실패: transactionId={}, error={}", 
                    transaction.getId(), e.getMessage());
            }
        }
        
        log.info("반복거래 알림 전송 완료: {}개", transactionsToday.size());
    }
}