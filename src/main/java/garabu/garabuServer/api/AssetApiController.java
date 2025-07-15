package garabu.garabuServer.api;

import garabu.garabuServer.domain.AssetType;
import garabu.garabuServer.dto.request.CreateAssetRequest;
import garabu.garabuServer.dto.request.UpdateAssetBalanceRequest;
import garabu.garabuServer.dto.request.UpdateAssetRequest;
import garabu.garabuServer.dto.response.AssetResponse;
import garabu.garabuServer.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 자산 관리 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v2/assets")
@Tag(name = "Asset API", description = "자산 관리 API")
public class AssetApiController {

    private final AssetService assetService;

    @Autowired
    public AssetApiController(AssetService assetService) {
        this.assetService = assetService;
    }

    /**
     * 자산 생성
     */
    @PostMapping("/books/{bookId}")
    @Operation(summary = "자산 생성", description = "가계부에 새로운 자산을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "자산 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음")
    })
    public ResponseEntity<AssetResponse> createAsset(
            @Parameter(description = "가계부 ID") @PathVariable Long bookId,
            @Valid @RequestBody CreateAssetRequest request) {
        
        AssetResponse response = assetService.createAsset(bookId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 가계부별 자산 목록 조회
     */
    @GetMapping("/books/{bookId}")
    @Operation(summary = "가계부별 자산 목록 조회", description = "특정 가계부의 모든 활성 자산을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음")
    })
    public ResponseEntity<Map<String, Object>> getAssetsByBook(
            @Parameter(description = "가계부 ID") @PathVariable Long bookId) {
        
        List<AssetResponse> assets = assetService.getAssetsByBook(bookId);
        return ResponseEntity.ok(Map.of("assets", assets));
    }

    /**
     * 가계부별 자산 타입별 자산 목록 조회
     */
    @GetMapping("/books/{bookId}/type/{assetType}")
    @Operation(summary = "자산 타입별 자산 목록 조회", description = "특정 가계부의 특정 타입 자산들을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음")
    })
    public ResponseEntity<Map<String, Object>> getAssetsByBookAndType(
            @Parameter(description = "가계부 ID") @PathVariable Long bookId,
            @Parameter(description = "자산 타입") @PathVariable AssetType assetType) {
        
        List<AssetResponse> assets = assetService.getAssetsByBookAndType(bookId, assetType);
        return ResponseEntity.ok(Map.of("assets", assets));
    }

    /**
     * 자산 상세 조회
     */
    @GetMapping("/{assetId}")
    @Operation(summary = "자산 상세 조회", description = "특정 자산의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "자산을 찾을 수 없음")
    })
    public ResponseEntity<AssetResponse> getAsset(
            @Parameter(description = "자산 ID") @PathVariable Long assetId) {
        
        AssetResponse response = assetService.getAsset(assetId);
        return ResponseEntity.ok(response);
    }

    /**
     * 자산 정보 수정
     */
    @PutMapping("/{assetId}")
    @Operation(summary = "자산 정보 수정", description = "자산의 기본 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "자산을 찾을 수 없음")
    })
    public ResponseEntity<AssetResponse> updateAsset(
            @Parameter(description = "자산 ID") @PathVariable Long assetId,
            @Valid @RequestBody UpdateAssetRequest request) {
        
        AssetResponse response = assetService.updateAsset(assetId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 자산 잔액 수정
     */
    @PatchMapping("/{assetId}/balance")
    @Operation(summary = "자산 잔액 수정", description = "자산의 잔액을 추가하거나 차감합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 잔액 부족"),
            @ApiResponse(responseCode = "404", description = "자산을 찾을 수 없음")
    })
    public ResponseEntity<AssetResponse> updateAssetBalance(
            @Parameter(description = "자산 ID") @PathVariable Long assetId,
            @Valid @RequestBody UpdateAssetBalanceRequest request) {
        
        AssetResponse response = assetService.updateAssetBalance(assetId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 자산 삭제 (비활성화)
     */
    @DeleteMapping("/{assetId}")
    @Operation(summary = "자산 삭제", description = "자산을 비활성화합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "자산을 찾을 수 없음")
    })
    public ResponseEntity<Void> deleteAsset(
            @Parameter(description = "자산 ID") @PathVariable Long assetId) {
        
        assetService.deleteAsset(assetId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 가계부별 총 자산 계산
     */
    @GetMapping("/books/{bookId}/total")
    @Operation(summary = "가계부별 총 자산 계산", description = "특정 가계부의 총 자산을 계산합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "계산 성공"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음")
    })
    public ResponseEntity<Map<String, Object>> getTotalAssets(
            @Parameter(description = "가계부 ID") @PathVariable Long bookId) {
        
        Long totalAssets = assetService.getTotalAssetsByBook(bookId);
        return ResponseEntity.ok(Map.of("totalAssets", totalAssets));
    }

    /**
     * 자산 타입 목록 조회
     */
    @GetMapping("/types")
    @Operation(summary = "자산 타입 목록 조회", description = "사용 가능한 모든 자산 타입을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<Map<String, Object>> getAssetTypes() {
        AssetType[] assetTypes = AssetType.values();
        return ResponseEntity.ok(Map.of("assetTypes", assetTypes));
    }
}