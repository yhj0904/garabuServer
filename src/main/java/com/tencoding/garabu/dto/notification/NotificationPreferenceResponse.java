package com.tencoding.garabu.dto.notification;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import garabu.garabuServer.domain.NotificationPreference;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceResponse {
    private Long id;
    private Long memberId;
    private Boolean pushEnabled;
    private Boolean emailEnabled;
    private Boolean transactionAlert;
    private Boolean budgetAlert;
    private Boolean goalAlert;
    private Boolean recurringAlert;
    private Boolean bookInviteAlert;
    private Boolean friendRequestAlert;
    private Boolean commentAlert;
    private Boolean weekendAlert;
    private String quietHoursStart;
    private String quietHoursEnd;
    
    public static NotificationPreferenceResponse fromEntity(NotificationPreference preference) {
        return NotificationPreferenceResponse.builder()
                .id(preference.getId())
                .memberId(preference.getMember().getId())
                .pushEnabled(preference.getPushEnabled())
                .emailEnabled(preference.getEmailEnabled())
                .transactionAlert(preference.getTransactionAlert())
                .budgetAlert(preference.getBudgetAlert())
                .goalAlert(preference.getGoalAlert())
                .recurringAlert(preference.getRecurringAlert())
                .bookInviteAlert(preference.getBookInviteAlert())
                .friendRequestAlert(preference.getFriendRequestAlert())
                .commentAlert(preference.getCommentAlert())
                .weekendAlert(preference.getWeekendAlert())
                .quietHoursStart(preference.getQuietHoursStart())
                .quietHoursEnd(preference.getQuietHoursEnd())
                .build();
    }
} 