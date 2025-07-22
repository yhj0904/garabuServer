package garabu.garabuServer.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리기
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 가계부 접근 권한 예외 처리
     */
    @ExceptionHandler(BookAccessException.class)
    public ResponseEntity<Map<String, Object>> handleBookAccessException(BookAccessException e) {
        log.warn("가계부 접근 권한 예외: {}", e.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.FORBIDDEN.value());
        errorResponse.put("error", "Forbidden");
        errorResponse.put("message", e.getMessage());
        errorResponse.put("path", "/api/v2/category");
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * 권한 부족 예외 처리
     */
    @ExceptionHandler(InsufficientPermissionException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientPermissionException(InsufficientPermissionException e) {
        log.warn("권한 부족 예외: {}", e.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.FORBIDDEN.value());
        errorResponse.put("error", "Insufficient Permission");
        errorResponse.put("message", e.getMessage());
        errorResponse.put("path", "/api/v2/category");
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * 엔티티를 찾을 수 없는 예외 처리
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFoundException(EntityNotFoundException e) {
        log.warn("엔티티 조회 실패: {}", e.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "Not Found");
        errorResponse.put("message", e.getMessage());
        errorResponse.put("path", "/api/v2/category");
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * 일반적인 IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("잘못된 요청 인수: {}", e.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Bad Request");
        errorResponse.put("message", e.getMessage());
        errorResponse.put("path", "/api/v2/category");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Bean Validation 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("입력 데이터 검증 실패: {}", e.getMessage());
        
        // 검증 실패 필드들을 정리하여 사용자에게 친화적인 메시지 생성
        String validationErrors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Validation Failed");
        errorResponse.put("message", "입력 데이터가 올바르지 않습니다: " + validationErrors);
        errorResponse.put("path", "/api/v2/ledger");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 가계부 조회 실패 예외 처리
     */
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleBookNotFoundException(BookNotFoundException e, WebRequest request) {
        log.warn("가계부 조회 실패: {}", e.getMessage());
        
        Map<String, Object> errorResponse = createErrorResponse(
            HttpStatus.NOT_FOUND, 
            "Book Not Found", 
            e.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * 중복 리소스 생성 예외 처리
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateResourceException(DuplicateResourceException e, WebRequest request) {
        log.warn("중복 리소스 생성 시도: {}", e.getMessage());
        
        Map<String, Object> errorResponse = createErrorResponse(
            HttpStatus.CONFLICT, 
            "Duplicate Resource", 
            e.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * 리소스를 찾을 수 없는 예외 처리 (잘못된 경로 포함)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFoundException(NoResourceFoundException e, WebRequest request) {
        String requestedPath = e.getResourcePath();
        log.warn("리소스를 찾을 수 없음: {}", requestedPath);
        
        // 중복된 /api/v2 경로 감지
        if (requestedPath != null && requestedPath.contains("/api/v2/api/v2")) {
            String correctPath = requestedPath.replace("/api/v2/api/v2", "/api/v2");
            String message = String.format("잘못된 경로입니다. 올바른 경로: %s (클라이언트에서 기본 경로와 엔드포인트 경로가 중복되었습니다)", correctPath);
            
            Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST, 
                "Invalid Path", 
                message,
                requestedPath
            );
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        Map<String, Object> errorResponse = createErrorResponse(
            HttpStatus.NOT_FOUND, 
            "Resource Not Found", 
            "요청한 리소스를 찾을 수 없습니다: " + requestedPath,
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * 기타 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e, WebRequest request) {
        log.error("예상치 못한 오류 발생: {}", e.getMessage(), e);
        
        Map<String, Object> errorResponse = createErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR, 
            "Internal Server Error", 
            "서버 내부 오류가 발생했습니다.",
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * 공통 에러 응답 생성 헬퍼 메서드
     */
    private Map<String, Object> createErrorResponse(HttpStatus status, String error, String message, String path) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        errorResponse.put("path", path);
        return errorResponse;
    }
}