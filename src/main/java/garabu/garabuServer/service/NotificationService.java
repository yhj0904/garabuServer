package garabu.garabuServer.service;

import garabu.garabuServer.domain.*;
import garabu.garabuServer.repository.*;
import com.tencoding.garabu.dto.notification.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
    
    private final MemberRepository memberRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final BudgetAlertRepository budgetAlertRepository;
    private final BudgetRepository budgetRepository;
    
    public NotificationPreferenceResponse getNotificationPreferences(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        
        NotificationPreference preference = notificationPreferenceRepository.findByMember(member)
                .orElseGet(() -> {
                    NotificationPreference newPreference = new NotificationPreference(member);
                    return notificationPreferenceRepository.save(newPreference);
                });
        
        return NotificationPreferenceResponse.fromEntity(preference);
    }
    
    @Transactional
    public NotificationPreferenceResponse updateNotificationPreferences(String email, NotificationPreferenceRequest request) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        
        NotificationPreference preference = notificationPreferenceRepository.findByMember(member)
                .orElseGet(() -> new NotificationPreference(member));
        
        preference.setPushEnabled(request.getPushEnabled());
        preference.setEmailEnabled(request.getEmailEnabled());
        preference.setTransactionAlert(request.getTransactionAlert());
        preference.setBudgetAlert(request.getBudgetAlert());
        preference.setGoalAlert(request.getGoalAlert());
        preference.setRecurringAlert(request.getRecurringAlert());
        preference.setBookInviteAlert(request.getBookInviteAlert());
        preference.setFriendRequestAlert(request.getFriendRequestAlert());
        preference.setCommentAlert(request.getCommentAlert());
        preference.setWeekendAlert(request.getWeekendAlert());
        preference.setQuietHoursStart(request.getQuietHoursStart());
        preference.setQuietHoursEnd(request.getQuietHoursEnd());
        
        NotificationPreference saved = notificationPreferenceRepository.save(preference);
        
        return NotificationPreferenceResponse.fromEntity(saved);
    }
    
    public List<BudgetAlertResponse> getBudgetAlerts(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        
        List<BudgetAlert> alerts = budgetAlertRepository.findByMemberOrderByCreatedAtDesc(member);
        
        return alerts.stream()
                .map(BudgetAlertResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public BudgetAlertResponse createBudgetAlert(String email, BudgetAlertRequest request) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        
        Budget budget = budgetRepository.findById(request.getBudgetId())
                .orElseThrow(() -> new IllegalArgumentException("예산을 찾을 수 없습니다."));
        
        // 권한 체크
        if (!budget.getBook().hasMember(member)) {
            throw new IllegalArgumentException("해당 가계부에 접근 권한이 없습니다.");
        }
        
        // 중복 체크
        if (budgetAlertRepository.existsByMemberAndBudget(member, budget)) {
            throw new IllegalArgumentException("이미 해당 예산에 대한 알림이 설정되어 있습니다.");
        }
        
        BudgetAlert alert = new BudgetAlert(budget, member, request.getThreshold());
        alert.setIsActive(request.getEnabled());
        
        BudgetAlert saved = budgetAlertRepository.save(alert);
        
        return BudgetAlertResponse.fromEntity(saved);
    }
    
    @Transactional
    public BudgetAlertResponse updateBudgetAlert(String email, Long alertId, BudgetAlertRequest request) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        
        BudgetAlert alert = budgetAlertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("예산 알림을 찾을 수 없습니다."));
        
        // 권한 체크
        if (!alert.getMember().equals(member)) {
            throw new IllegalArgumentException("해당 알림에 대한 권한이 없습니다.");
        }
        
        alert.setAlertThreshold(request.getThreshold());
        alert.setIsActive(request.getEnabled());
        
        BudgetAlert saved = budgetAlertRepository.save(alert);
        
        return BudgetAlertResponse.fromEntity(saved);
    }
    
    @Transactional
    public void deleteBudgetAlert(String email, Long alertId) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        
        BudgetAlert alert = budgetAlertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("예산 알림을 찾을 수 없습니다."));
        
        // 권한 체크
        if (!alert.getMember().equals(member)) {
            throw new IllegalArgumentException("해당 알림에 대한 권한이 없습니다.");
        }
        
        budgetAlertRepository.delete(alert);
    }
    
    // 반복거래 알림 메서드 추가
    public void sendRecurringTransactionNotification(Long memberId, String transactionName, Integer amount) {
        try {
            Member member = memberRepository.findOne(memberId);
            if (member == null) {
                throw new IllegalArgumentException("회원을 찾을 수 없습니다.");
            }
            
            NotificationPreference preference = notificationPreferenceRepository.findByMember(member)
                    .orElseGet(() -> new NotificationPreference(member));
            
            if (!preference.getRecurringAlert() || !preference.getPushEnabled()) {
                return;
            }
            
            String title = "반복거래 자동 실행";
            String body = String.format("%s ₩%,d이(가) 자동으로 실행되었습니다.", 
                transactionName, amount);
            
            // FCM 푸시 알림 전송 로직 추가 필요
            log.info("반복거래 알림 전송: memberId={}, transaction={}", memberId, transactionName);
            
        } catch (Exception e) {
            log.error("반복거래 알림 전송 실패: memberId={}, error={}", memberId, e.getMessage());
        }
    }
    
    public void sendRecurringTransactionReminder(Long memberId, String transactionName, Integer amount) {
        try {
            Member member = memberRepository.findOne(memberId);
            if (member == null) {
                throw new IllegalArgumentException("회원을 찾을 수 없습니다.");
            }
            
            NotificationPreference preference = notificationPreferenceRepository.findByMember(member)
                    .orElseGet(() -> new NotificationPreference(member));
            
            if (!preference.getRecurringAlert() || !preference.getPushEnabled()) {
                return;
            }
            
            String title = "오늘의 반복거래";
            String body = String.format("%s ₩%,d이(가) 오늘 실행 예정입니다.", 
                transactionName, amount);
            
            // FCM 푸시 알림 전송 로직 추가 필요
            log.info("반복거래 사전 알림 전송: memberId={}, transaction={}", memberId, transactionName);
            
        } catch (Exception e) {
            log.error("반복거래 사전 알림 전송 실패: memberId={}, error={}", memberId, e.getMessage());
        }
    }
} 