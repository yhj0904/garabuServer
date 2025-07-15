package garabu.garabuServer.domain;

/**
 * 자산 타입 열거형
 * 모든 유저가 공통으로 사용할 수 있는 기본 자산 타입들
 */
public enum AssetType {
    CASH("현금"),
    SAVINGS_ACCOUNT("저축예금"),
    CHECKING_ACCOUNT("당좌예금"),
    CREDIT_CARD("신용카드"),
    DEBIT_CARD("체크카드"),
    INVESTMENT("투자자산"),
    REAL_ESTATE("부동산"),
    OTHER("기타");

    private final String displayName;

    AssetType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}