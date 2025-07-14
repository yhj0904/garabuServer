package garabu.garabuServer.api;

import garabu.garabuServer.service.TestDataSqlGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 테스트 데이터 SQL 스크립트 생성 API 컨트롤러
 * 
 * 데이터베이스에 직접 삽입할 수 있는 SQL 스크립트를 생성
 * 
 * @author Garabu Team
 * @version 1.0
 */
@Tag(name = "테스트 데이터 SQL", description = "테스트 데이터 SQL 스크립트 생성 API")
@RestController
@RequestMapping("/api/v2/test-data/sql")
@RequiredArgsConstructor
@Slf4j
public class TestDataSqlController {

    private final TestDataSqlGenerator sqlGenerator;

    /**
     * Ledger 테스트 데이터 SQL 스크립트 생성 및 다운로드
     */
    @Operation(summary = "Ledger 테스트 데이터 SQL 스크립트 생성", 
               description = "지정된 개수의 Ledger 테스트 데이터를 SQL 스크립트로 생성하고 다운로드합니다.")
    @PostMapping("/ledger/generate")
    public ResponseEntity<Resource> generateLedgerSqlScript(
            @Parameter(description = "가계부 ID", required = true)
            @RequestParam Long bookId,
            @Parameter(description = "생성할 데이터 개수", required = true)
            @RequestParam int count) {
        
        log.info("Ledger SQL 스크립트 생성 요청 - 가계부 ID: {}, 개수: {}", bookId, count);
        
        try {
            // 최대 100만 개로 제한
            if (count > 1000000) {
                return ResponseEntity.badRequest().build();
            }
            
            // 임시 파일 생성
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("ledger_testdata_%s_book%d_%d.sql", timestamp, bookId, count);
            String outputPath = System.getProperty("java.io.tmpdir") + File.separator + filename;
            
            // SQL 스크립트 생성
            sqlGenerator.generateLedgerSqlScript(count, bookId, outputPath);
            
            // 파일 다운로드 응답
            File file = new File(outputPath);
            Resource resource = new FileSystemResource(file);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
            
        } catch (Exception e) {
            log.error("Ledger SQL 스크립트 생성 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 완전한 테스트 데이터 SQL 스크립트 생성 및 다운로드
     */
    @Operation(summary = "완전한 테스트 데이터 SQL 스크립트 생성", 
               description = "Member, Book, UserBook 등 모든 테스트 데이터를 포함한 완전한 SQL 스크립트를 생성합니다.")
    @PostMapping("/complete/generate")
    public ResponseEntity<Resource> generateCompleteTestDataSql() {
        
        log.info("완전한 테스트 데이터 SQL 스크립트 생성 요청");
        
        try {
            // 임시 파일 생성
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("complete_testdata_%s.sql", timestamp);
            String outputPath = System.getProperty("java.io.tmpdir") + File.separator + filename;
            
            // SQL 스크립트 생성
            sqlGenerator.generateCompleteTestDataSql(outputPath);
            
            // 파일 다운로드 응답
            File file = new File(outputPath);
            Resource resource = new FileSystemResource(file);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
            
        } catch (Exception e) {
            log.error("완전한 테스트 데이터 SQL 스크립트 생성 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 50,000건 표준 테스트 데이터 SQL 스크립트 생성
     */
    @Operation(summary = "50,000건 표준 테스트 데이터 SQL 스크립트 생성", 
               description = "성능 테스트를 위한 50,000건 표준 테스트 데이터 SQL 스크립트를 생성합니다.")
    @PostMapping("/standard-50k")
    public ResponseEntity<Resource> generateStandard50kTestData(
            @Parameter(description = "가계부 ID (기본값: 1)", required = false)
            @RequestParam(defaultValue = "1") Long bookId) {
        
        log.info("50,000건 표준 테스트 데이터 SQL 스크립트 생성 요청 - 가계부 ID: {}", bookId);
        
        try {
            // 임시 파일 생성
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("standard_50k_testdata_%s_book%d.sql", timestamp, bookId);
            String outputPath = System.getProperty("java.io.tmpdir") + File.separator + filename;
            
            // 50,000건 SQL 스크립트 생성
            sqlGenerator.generateLedgerSqlScript(50000, bookId, outputPath);
            
            // 파일 다운로드 응답
            File file = new File(outputPath);
            Resource resource = new FileSystemResource(file);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
            
        } catch (Exception e) {
            log.error("50,000건 표준 테스트 데이터 SQL 스크립트 생성 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * SQL 스크립트 생성 상태 확인
     */
    @Operation(summary = "SQL 스크립트 생성 가능 여부 확인", 
               description = "SQL 스크립트 생성에 필요한 기본 데이터가 존재하는지 확인합니다.")
    @GetMapping("/status")
    public ResponseEntity<SqlGenerationStatus> checkSqlGenerationStatus() {
        
        try {
            // 기본 데이터 존재 여부 확인 로직
            // 실제 구현에서는 데이터베이스에서 필요한 데이터 존재 여부 확인
            
            return ResponseEntity.ok(new SqlGenerationStatus(
                true,
                "SQL 스크립트 생성 가능",
                System.getProperty("java.io.tmpdir"),
                "최대 100만 건까지 생성 가능"
            ));
            
        } catch (Exception e) {
            log.error("SQL 스크립트 생성 상태 확인 실패", e);
            return ResponseEntity.ok(new SqlGenerationStatus(
                false,
                "SQL 스크립트 생성 불가능: " + e.getMessage(),
                null,
                null
            ));
        }
    }
    
    /**
     * SQL 스크립트 생성 상태 응답 DTO
     */
    public static class SqlGenerationStatus {
        private boolean available;
        private String message;
        private String tempDirectory;
        private String limitations;
        
        public SqlGenerationStatus(boolean available, String message, String tempDirectory, String limitations) {
            this.available = available;
            this.message = message;
            this.tempDirectory = tempDirectory;
            this.limitations = limitations;
        }
        
        public boolean isAvailable() { return available; }
        public String getMessage() { return message; }
        public String getTempDirectory() { return tempDirectory; }
        public String getLimitations() { return limitations; }
    }
}