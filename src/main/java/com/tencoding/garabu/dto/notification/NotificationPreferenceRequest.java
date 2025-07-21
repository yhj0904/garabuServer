package com.tencoding.garabu.dto.notification;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class NotificationPreferenceRequest {
    @NotNull(message = "푸시 알림 설정은 필수입니다")
    private Boolean pushEnabled;
    
    @NotNull(message = "이메일 알림 설정은 필수입니다")
    private Boolean emailEnabled;
    
    @NotNull(message = "트랜잭션 알림 설정은 필수입니다")
    private Boolean transactionAlert;
    
    @NotNull(message = "예산 알림 설정은 필수입니다")
    private Boolean budgetAlert;
    
    @NotNull(message = "목표 알림 설정은 필수입니다")
    private Boolean goalAlert;
    
    @NotNull(message = "반복 거래 알림 설정은 필수입니다")
    private Boolean recurringAlert;
    
    @NotNull(message = "가계부 초대 알림 설정은 필수입니다")
    private Boolean bookInviteAlert;
    
    @NotNull(message = "친구 요청 알림 설정은 필수입니다")
    private Boolean friendRequestAlert;
    
    @NotNull(message = "댓글 알림 설정은 필수입니다")
    private Boolean commentAlert;
    
    @NotNull(message = "주말 알림 설정은 필수입니다")
    private Boolean weekendAlert;
    
    private String quietHoursStart; // HH:mm 형식
    private String quietHoursEnd; // HH:mm 형식
} 