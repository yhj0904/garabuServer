package garabu.garabuServer.domain;

import lombok.Getter;

@Getter
public enum GoalStatus {
    ACTIVE("진행중", "목표가 활성화되어 진행 중"),
    PAUSED("일시정지", "목표가 일시적으로 중단됨"),
    COMPLETED("완료", "목표를 달성함"),
    CANCELLED("취소", "목표를 취소함"),
    EXPIRED("만료", "목표 기한이 지남");
    
    private final String displayName;
    private final String description;
    
    GoalStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
} 