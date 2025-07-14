package garabu.garabuServer.exception;

/**
 * 중복된 리소스 생성 시 발생하는 예외
 * HTTP 409 Conflict로 처리됨
 */
public class DuplicateResourceException extends RuntimeException {
    
    public DuplicateResourceException(String message) {
        super(message);
    }
    
    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}