package com.tencoding.garabu.dto.notification;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import garabu.garabuServer.domain.BudgetAlert;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetAlertResponse {
    private Long id;
    private Long memberId;
    private Long budgetId;
    private String budgetName;
    private Integer threshold;
    private Boolean enabled;
    private LocalDateTime lastNotifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static BudgetAlertResponse fromEntity(BudgetAlert alert) {
        return BudgetAlertResponse.builder()
                .id(alert.getId())
                .memberId(alert.getMember().getId())
                .budgetId(alert.getBudget().getId())
                .budgetName(alert.getBudget().getBudgetMonth())
                .threshold(alert.getAlertThreshold())
                .enabled(alert.getIsActive())
                .lastNotifiedAt(alert.getLastAlertSentAt())
                .createdAt(alert.getCreatedAt())
                .updatedAt(alert.getUpdatedAt())
                .build();
    }
} 