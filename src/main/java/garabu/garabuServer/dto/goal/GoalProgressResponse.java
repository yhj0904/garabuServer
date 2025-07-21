package garabu.garabuServer.dto.goal;

import garabu.garabuServer.domain.GoalStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GoalProgressResponse {
    private Long goalId;
    private BigDecimal currentAmount;
    private BigDecimal targetAmount;
    private BigDecimal progressPercentage;
    private GoalStatus status;
    private LocalDateTime completedAt;
} 