package garabu.garabuServer.dto.goal;

import garabu.garabuServer.domain.GoalType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AchievementResponse {
    private Long id;
    private String name;
    private GoalType goalType;
    private BigDecimal targetAmount;
    private LocalDateTime completedAt;
    private Long daysTaken;
    private String icon;
    private String color;
} 