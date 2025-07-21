package garabu.garabuServer.domain;

import lombok.Getter;

@Getter
public enum RecurrenceType {
    DAILY("매일", "일 단위로 반복"),
    WEEKLY("매주", "주 단위로 반복"),
    MONTHLY("매월", "월 단위로 반복"),
    YEARLY("매년", "년 단위로 반복");
    
    private final String displayName;
    private final String description;
    
    RecurrenceType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
} 