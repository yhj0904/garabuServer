package garabu.garabuServer.dto.goal;

import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateGoalRequest {
    
    private String name;
    
    private String description;
    
    @Positive(message = "목표 금액은 양수여야 합니다")
    private BigDecimal targetAmount;
    
    private LocalDate targetDate;
    
    private String category;
    
    private String icon;
    
    private String color;
} 