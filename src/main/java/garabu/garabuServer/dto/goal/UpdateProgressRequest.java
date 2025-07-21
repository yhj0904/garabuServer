package garabu.garabuServer.dto.goal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateProgressRequest {
    
    @NotNull(message = "금액은 필수입니다")
    @PositiveOrZero(message = "금액은 0 이상이어야 합니다")
    private BigDecimal amount;
    
    @NotNull(message = "추가 여부는 필수입니다")
    private Boolean isAdditive = false; // true: 추가, false: 전체 업데이트
} 