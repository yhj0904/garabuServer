package garabu.garabuServer.domain;

/**
 * 가계부 내 사용자 역할 정의
 */
public enum BookRole {
    OWNER,   // 가계부 소유자 (모든 권한)
    EDITOR,  // 편집자 (읽기, 쓰기 권한)
    VIEWER   // 조회자 (읽기 권한만)
}
