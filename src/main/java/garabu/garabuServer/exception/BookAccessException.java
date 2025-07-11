package garabu.garabuServer.exception;

/**
 * 가계부 접근 권한 관련 예외
 */
public class BookAccessException extends RuntimeException {
    
    public BookAccessException(String message) {
        super(message);
    }
    
    public BookAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}