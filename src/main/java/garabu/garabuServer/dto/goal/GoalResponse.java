package garabu.garabuServer.dto.goal;

import garabu.garabuServer.domain.GoalStatus;
import garabu.garabuServer.domain.GoalType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class GoalResponse {
    private Long id;
    private String name;
    private String description;
    private GoalType goalType;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate startDate;
    private LocalDate targetDate;
    private GoalStatus status;
    private String category;
    private String icon;
    private String color;
    private BigDecimal progressPercentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 