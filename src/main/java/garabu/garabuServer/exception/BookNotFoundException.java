package garabu.garabuServer.exception;

/**
 * 가계부를 찾을 수 없을 때 발생하는 예외
 * HTTP 404 Not Found로 처리됨
 */
public class BookNotFoundException extends RuntimeException {
    
    public BookNotFoundException(String message) {
        super(message);
    }
    
    public BookNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public BookNotFoundException(Long bookId) {
        super("가계부를 찾을 수 없습니다. ID: " + bookId);
    }
}