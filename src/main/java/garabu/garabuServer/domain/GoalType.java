package garabu.garabuServer.domain;

import lombok.Getter;

@Getter
public enum GoalType {
    SAVING("저축", "정기적인 저축 목표"),
    SPENDING_REDUCTION("지출 절감", "특정 카테고리의 지출을 줄이는 목표"),
    DEBT_REPAYMENT("부채 상환", "대출이나 빚을 갚는 목표"),
    EMERGENCY_FUND("비상금", "비상 상황을 위한 자금 마련"),
    INVESTMENT("투자", "투자 목표 금액 달성"),
    PURCHASE("구매", "특정 물건 구매를 위한 저축"),
    TRAVEL("여행", "여행 자금 마련"),
    EDUCATION("교육", "교육비 마련"),
    RETIREMENT("은퇴", "은퇴 자금 준비"),
    CUSTOM("기타", "사용자 정의 목표");
    
    private final String displayName;
    private final String description;
    
    GoalType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
} 