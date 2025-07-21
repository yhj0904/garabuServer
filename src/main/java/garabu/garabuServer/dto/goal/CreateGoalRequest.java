package garabu.garabuServer.dto.goal;

import garabu.garabuServer.domain.GoalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateGoalRequest {
    
    @NotBlank(message = "목표 이름은 필수입니다")
    private String name;
    
    private String description;
    
    @NotNull(message = "목표 타입은 필수입니다")
    private GoalType goalType;
    
    @NotNull(message = "목표 금액은 필수입니다")
    @Positive(message = "목표 금액은 양수여야 합니다")
    private BigDecimal targetAmount;
    
    private BigDecimal currentAmount;
    
    @NotNull(message = "시작일은 필수입니다")
    private LocalDate startDate;
    
    @NotNull(message = "목표일은 필수입니다")
    private LocalDate targetDate;
    
    private String category;
    
    private String icon;
    
    private String color;
} 