package garabu.garabuServer.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 기본 제공 카테고리 열거형
 *
 * <p>모든 사용자에게 공통으로 제공되는 기본 카테고리 목록입니다.
 * 이미지에서 확인한 카테고리들을 포함하여 정의합니다.</p>
 */
@Schema(description = "기본 제공 카테고리 열거형")
public enum DefaultCategory {
    
    // 식품 관련
    FOOD("식비", "🍽️"),
    
    // 교통 관련
    TRANSPORTATION("교통/차량", "🚗"),
    
    // 문화생활
    CULTURE("문화생활", "🎭"),
    
    // 마트/편의점
    MART("마트/편의점", "🛒"),
    
    // 패션/미용
    FASHION("패션/미용", "👗"),
    
    // 생활용품
    HOUSEHOLD("생활용품", "🪑"),
    
    // 주거/통신
    HOUSING("주거/통신", "🏠"),
    
    // 건강
    HEALTH("건강", "👨‍⚕️"),
    
    // 교육
    EDUCATION("교육", "📚"),
    
    // 경조사/회비
    EVENTS("경조사/회비", "🎁"),
    
    // 부모님
    PARENTS("부모님", "👨‍👩‍👧‍👦"),
    
    // 기타
    OTHER("기타", "📋"),
    
    // 추가 일반적인 카테고리들
    SALARY("급여", "💰"),
    ALLOWANCE("용돈", "💳"),
    INVESTMENT("투자", "📈"),
    INSURANCE("보험", "🛡️"),
    MEDICAL("의료", "🏥"),
    SUBSCRIPTION("구독", "📱"),
    GIFT("선물", "🎁"),
    TRAVEL("여행", "✈️"),
    CAFE("카페", "☕"),
    DELIVERY("배달", "🚚");
    
    private final String displayName;
    private final String emoji;
    
    DefaultCategory(String displayName, String emoji) {
        this.displayName = displayName;
        this.emoji = emoji;
    }
    
    /**
     * 카테고리 표시명을 반환합니다.
     *
     * @return 카테고리 표시명
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 카테고리 이모지를 반환합니다.
     *
     * @return 카테고리 이모지
     */
    public String getEmoji() {
        return emoji;
    }
    
    /**
     * 이모지와 함께 표시명을 반환합니다.
     *
     * @return 이모지 + 표시명
     */
    public String getDisplayNameWithEmoji() {
        return emoji + " " + displayName;
    }
}