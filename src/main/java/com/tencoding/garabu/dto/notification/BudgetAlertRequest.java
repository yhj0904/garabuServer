package com.tencoding.garabu.dto.notification;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@Data
public class BudgetAlertRequest {
    @NotNull(message = "예산 ID는 필수입니다")
    private Long budgetId;
    
    @NotNull(message = "알림 임계값은 필수입니다")
    @Min(value = 50, message = "알림 임계값은 50% 이상이어야 합니다")
    @Max(value = 100, message = "알림 임계값은 100% 이하여야 합니다")
    private Integer threshold;
    
    @NotNull(message = "알림 활성화 여부는 필수입니다")
    private Boolean enabled;
} 