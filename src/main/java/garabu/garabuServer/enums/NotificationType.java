package garabu.garabuServer.enums;

/**
 * 알림 타입 열거형
 */
public enum NotificationType {
    /**
     * 새 거래 내역 추가
     */
    NEW_TRANSACTION("NEW_TRANSACTION", "새 거래 추가", "새로운 거래 내역이 추가되었습니다."),
    
    /**
     * 가계부 멤버 초대
     */
    BOOK_INVITATION("BOOK_INVITATION", "가계부 초대", "가계부에 초대되었습니다."),
    
    /**
     * 가계부 멤버 제거
     */
    MEMBER_REMOVED("MEMBER_REMOVED", "멤버 제거", "가계부에서 제거되었습니다."),
    
    /**
     * 가계부 권한 변경
     */
    ROLE_CHANGED("ROLE_CHANGED", "권한 변경", "가계부 권한이 변경되었습니다."),
    
    /**
     * 가계부 멤버 탈퇴
     */
    MEMBER_LEFT("MEMBER_LEFT", "멤버 탈퇴", "가계부 멤버가 탈퇴했습니다."),
    
    /**
     * 예산 초과 알림
     */
    BUDGET_EXCEEDED("BUDGET_EXCEEDED", "예산 초과", "예산을 초과했습니다."),
    
    /**
     * 예산 80% 도달 알림
     */
    BUDGET_WARNING("BUDGET_WARNING", "예산 경고", "예산의 80%에 도달했습니다."),
    
    /**
     * 시스템 공지사항
     */
    SYSTEM_NOTICE("SYSTEM_NOTICE", "시스템 공지", "시스템 공지사항입니다."),
    
    /**
     * 앱 업데이트 알림
     */
    APP_UPDATE("APP_UPDATE", "앱 업데이트", "새로운 앱 업데이트가 있습니다."),
    
    /**
     * 백업 완료 알림
     */
    BACKUP_COMPLETED("BACKUP_COMPLETED", "백업 완료", "데이터 백업이 완료되었습니다."),
    
    /**
     * 정기 리포트 알림
     */
    MONTHLY_REPORT("MONTHLY_REPORT", "월간 리포트", "이번 달 가계부 리포트가 준비되었습니다."),
    
    /**
     * 기타 알림
     */
    GENERAL("GENERAL", "일반 알림", "일반 알림입니다.");
    
    private final String code;
    private final String title;
    private final String defaultMessage;
    
    NotificationType(String code, String title, String defaultMessage) {
        this.code = code;
        this.title = title;
        this.defaultMessage = defaultMessage;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getDefaultMessage() {
        return defaultMessage;
    }
    
    /**
     * 코드로 알림 타입 찾기
     * 
     * @param code 알림 타입 코드
     * @return 알림 타입
     */
    public static NotificationType fromCode(String code) {
        for (NotificationType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return GENERAL;
    }
}