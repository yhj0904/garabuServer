package garabu.garabuServer.service;

import garabu.garabuServer.enums.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 알림 템플릿 서비스
 * 
 * 다양한 알림 상황에 맞는 제목과 내용을 생성합니다.
 */
@Service
@Slf4j
public class NotificationTemplateService {
    
    /**
     * 새 거래 내역 알림 템플릿
     * 
     * @param params 파라미터 맵 (authorName, bookTitle, transactionType, description, amount)
     * @return 알림 템플릿
     */
    public NotificationTemplate createNewTransactionTemplate(Map<String, Object> params) {
        String authorName = (String) params.get("authorName");
        String bookTitle = (String) params.get("bookTitle");
        String transactionType = (String) params.get("transactionType");
        String description = (String) params.get("description");
        Integer amount = (Integer) params.get("amount");
        
        String title = String.format("새 %s 추가", transactionType);
        String body = String.format("%s님이 \"%s\" 가계부에 새 %s을 추가했습니다: %s (₩%,d)", 
            authorName, bookTitle, transactionType, description, amount);
        
        return new NotificationTemplate(NotificationType.NEW_TRANSACTION, title, body, "open_transaction_detail");
    }
    
    /**
     * 가계부 초대 알림 템플릿
     * 
     * @param params 파라미터 맵 (inviterName, bookTitle, role)
     * @return 알림 템플릿
     */
    public NotificationTemplate createBookInvitationTemplate(Map<String, Object> params) {
        String inviterName = (String) params.get("inviterName");
        String bookTitle = (String) params.get("bookTitle");
        String role = (String) params.get("role");
        
        String roleText = "EDITOR".equals(role) ? "편집자" : "조회자";
        String title = "가계부 초대";
        String body = String.format("%s님이 \"%s\" 가계부에 %s로 초대했습니다.", 
            inviterName, bookTitle, roleText);
        
        return new NotificationTemplate(NotificationType.BOOK_INVITATION, title, body, "open_book_detail");
    }
    
    /**
     * 멤버 제거 알림 템플릿
     * 
     * @param params 파라미터 맵 (removerName, bookTitle)
     * @return 알림 템플릿
     */
    public NotificationTemplate createMemberRemovedTemplate(Map<String, Object> params) {
        String removerName = (String) params.get("removerName");
        String bookTitle = (String) params.get("bookTitle");
        
        String title = "가계부 멤버 제거";
        String body = String.format("%s님이 \"%s\" 가계부에서 회원님을 제거했습니다.", 
            removerName, bookTitle);
        
        return new NotificationTemplate(NotificationType.MEMBER_REMOVED, title, body, "open_book_list");
    }
    
    /**
     * 권한 변경 알림 템플릿
     * 
     * @param params 파라미터 맵 (changerName, bookTitle, newRole)
     * @return 알림 템플릿
     */
    public NotificationTemplate createRoleChangedTemplate(Map<String, Object> params) {
        String changerName = (String) params.get("changerName");
        String bookTitle = (String) params.get("bookTitle");
        String newRole = (String) params.get("newRole");
        
        String roleText = "EDITOR".equals(newRole) ? "편집자" : "조회자";
        String title = "가계부 권한 변경";
        String body = String.format("%s님이 \"%s\" 가계부에서 회원님의 권한을 %s로 변경했습니다.", 
            changerName, bookTitle, roleText);
        
        return new NotificationTemplate(NotificationType.ROLE_CHANGED, title, body, "open_book_detail");
    }
    
    /**
     * 멤버 탈퇴 알림 템플릿
     * 
     * @param params 파라미터 맵 (leftMemberName, bookTitle)
     * @return 알림 템플릿
     */
    public NotificationTemplate createMemberLeftTemplate(Map<String, Object> params) {
        String leftMemberName = (String) params.get("leftMemberName");
        String bookTitle = (String) params.get("bookTitle");
        
        String title = "가계부 멤버 탈퇴";
        String body = String.format("%s님이 \"%s\" 가계부에서 탈퇴했습니다.", 
            leftMemberName, bookTitle);
        
        return new NotificationTemplate(NotificationType.MEMBER_LEFT, title, body, "open_book_detail");
    }
    
    /**
     * 예산 초과 알림 템플릿
     * 
     * @param params 파라미터 맵 (bookTitle, percentage, currentAmount, budgetAmount)
     * @return 알림 템플릿
     */
    public NotificationTemplate createBudgetExceededTemplate(Map<String, Object> params) {
        String bookTitle = (String) params.get("bookTitle");
        Integer percentage = (Integer) params.get("percentage");
        Integer currentAmount = (Integer) params.get("currentAmount");
        Integer budgetAmount = (Integer) params.get("budgetAmount");
        
        String title = "예산 초과 알림";
        String body = String.format("\"%s\" 가계부의 이번 달 예산 %d%%를 사용했습니다. (₩%,d/₩%,d)", 
            bookTitle, percentage, currentAmount, budgetAmount);
        
        return new NotificationTemplate(NotificationType.BUDGET_EXCEEDED, title, body, "open_budget_detail");
    }
    
    /**
     * 예산 경고 알림 템플릿
     * 
     * @param params 파라미터 맵 (bookTitle, percentage, currentAmount, budgetAmount)
     * @return 알림 템플릿
     */
    public NotificationTemplate createBudgetWarningTemplate(Map<String, Object> params) {
        String bookTitle = (String) params.get("bookTitle");
        Integer percentage = (Integer) params.get("percentage");
        Integer currentAmount = (Integer) params.get("currentAmount");
        Integer budgetAmount = (Integer) params.get("budgetAmount");
        
        String title = "예산 경고";
        String body = String.format("\"%s\" 가계부의 이번 달 예산 %d%%를 사용했습니다. (₩%,d/₩%,d)", 
            bookTitle, percentage, currentAmount, budgetAmount);
        
        return new NotificationTemplate(NotificationType.BUDGET_WARNING, title, body, "open_budget_detail");
    }
    
    /**
     * 월간 리포트 알림 템플릿
     * 
     * @param params 파라미터 맵 (bookTitle, month, totalIncome, totalExpense)
     * @return 알림 템플릿
     */
    public NotificationTemplate createMonthlyReportTemplate(Map<String, Object> params) {
        String bookTitle = (String) params.get("bookTitle");
        String month = (String) params.get("month");
        Integer totalIncome = (Integer) params.get("totalIncome");
        Integer totalExpense = (Integer) params.get("totalExpense");
        
        String title = "월간 리포트";
        String body = String.format("\"%s\" 가계부의 %s 리포트가 준비되었습니다. 수입: ₩%,d, 지출: ₩%,d", 
            bookTitle, month, totalIncome, totalExpense);
        
        return new NotificationTemplate(NotificationType.MONTHLY_REPORT, title, body, "open_monthly_report");
    }
    
    /**
     * 시스템 공지 알림 템플릿
     * 
     * @param params 파라미터 맵 (title, message)
     * @return 알림 템플릿
     */
    public NotificationTemplate createSystemNoticeTemplate(Map<String, Object> params) {
        String title = (String) params.get("title");
        String message = (String) params.get("message");
        
        return new NotificationTemplate(NotificationType.SYSTEM_NOTICE, title, message, "open_notice_detail");
    }
    
    /**
     * 앱 업데이트 알림 템플릿
     * 
     * @param params 파라미터 맵 (version, features)
     * @return 알림 템플릿
     */
    public NotificationTemplate createAppUpdateTemplate(Map<String, Object> params) {
        String version = (String) params.get("version");
        String features = (String) params.get("features");
        
        String title = "앱 업데이트";
        String body = String.format("가라부 앱 %s 버전이 출시되었습니다. %s", version, features);
        
        return new NotificationTemplate(NotificationType.APP_UPDATE, title, body, "open_app_store");
    }
    
    /**
     * 백업 완료 알림 템플릿
     * 
     * @param params 파라미터 맵 (backupDate, dataCount)
     * @return 알림 템플릿
     */
    public NotificationTemplate createBackupCompletedTemplate(Map<String, Object> params) {
        String backupDate = (String) params.get("backupDate");
        Integer dataCount = (Integer) params.get("dataCount");
        
        String title = "백업 완료";
        String body = String.format("%s 데이터 백업이 완료되었습니다. (%,d개 항목)", backupDate, dataCount);
        
        return new NotificationTemplate(NotificationType.BACKUP_COMPLETED, title, body, "open_backup_detail");
    }
    
    /**
     * 일반 알림 템플릿
     * 
     * @param params 파라미터 맵 (title, body, action)
     * @return 알림 템플릿
     */
    public NotificationTemplate createGeneralTemplate(Map<String, Object> params) {
        String title = (String) params.get("title");
        String body = (String) params.get("body");
        String action = (String) params.getOrDefault("action", "open_main");
        
        return new NotificationTemplate(NotificationType.GENERAL, title, body, action);
    }
    
    /**
     * 알림 템플릿 생성 (타입별)
     * 
     * @param type 알림 타입
     * @param params 파라미터 맵
     * @return 알림 템플릿
     */
    public NotificationTemplate createTemplate(NotificationType type, Map<String, Object> params) {
        switch (type) {
            case NEW_TRANSACTION:
                return createNewTransactionTemplate(params);
            case BOOK_INVITATION:
                return createBookInvitationTemplate(params);
            case MEMBER_REMOVED:
                return createMemberRemovedTemplate(params);
            case ROLE_CHANGED:
                return createRoleChangedTemplate(params);
            case MEMBER_LEFT:
                return createMemberLeftTemplate(params);
            case BUDGET_EXCEEDED:
                return createBudgetExceededTemplate(params);
            case BUDGET_WARNING:
                return createBudgetWarningTemplate(params);
            case MONTHLY_REPORT:
                return createMonthlyReportTemplate(params);
            case SYSTEM_NOTICE:
                return createSystemNoticeTemplate(params);
            case APP_UPDATE:
                return createAppUpdateTemplate(params);
            case BACKUP_COMPLETED:
                return createBackupCompletedTemplate(params);
            case GENERAL:
            default:
                return createGeneralTemplate(params);
        }
    }
    
    /**
     * 헬퍼 메서드: 파라미터 맵 생성
     * 
     * @return 빈 파라미터 맵
     */
    public static Map<String, Object> createParams() {
        return new HashMap<>();
    }
    
    /**
     * 헬퍼 메서드: 파라미터 추가
     * 
     * @param params 파라미터 맵
     * @param key 키
     * @param value 값
     * @return 파라미터 맵
     */
    public static Map<String, Object> addParam(Map<String, Object> params, String key, Object value) {
        params.put(key, value);
        return params;
    }
    
    /**
     * 알림 템플릿 클래스
     */
    public static class NotificationTemplate {
        private final NotificationType type;
        private final String title;
        private final String body;
        private final String action;
        
        public NotificationTemplate(NotificationType type, String title, String body, String action) {
            this.type = type;
            this.title = title;
            this.body = body;
            this.action = action;
        }
        
        public NotificationType getType() { return type; }
        public String getTitle() { return title; }
        public String getBody() { return body; }
        public String getAction() { return action; }
    }
}