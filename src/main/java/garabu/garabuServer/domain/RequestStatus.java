package garabu.garabuServer.domain;

/**
 * 가계부 참가 요청 상태
 */
public enum RequestStatus {
    PENDING,    // 대기중
    ACCEPTED,   // 수락됨
    REJECTED    // 거절됨
} 