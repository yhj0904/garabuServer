package garabu.garabuServer.api;

import garabu.garabuServer.domain.GoalType;
import garabu.garabuServer.dto.goal.*;
import garabu.garabuServer.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/goals")
@RequiredArgsConstructor
@Tag(name = "Goal", description = "재정 목표 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class GoalApiController {
    
    private final GoalService goalService;
    
    @PostMapping("/books/{bookId}")
    @Operation(summary = "목표 생성", description = "새로운 재정 목표를 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "목표 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<GoalResponse> createGoal(
            @Parameter(description = "가계부 ID") @PathVariable Long bookId,
            @Valid @RequestBody CreateGoalRequest request) {
        
        GoalResponse response = goalService.createGoal(bookId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/books/{bookId}")
    @Operation(summary = "목표 목록 조회", description = "가계부의 목표 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<List<GoalResponse>> getGoals(
            @Parameter(description = "가계부 ID") @PathVariable Long bookId,
            @Parameter(description = "목표 타입") @RequestParam(required = false) GoalType type,
            @Parameter(description = "활성 상태만 조회") @RequestParam(required = false) Boolean active) {
        
        List<GoalResponse> goals = goalService.getGoals(bookId, type, active);
        return ResponseEntity.ok(goals);
    }
    
    @GetMapping("/{goalId}")
    @Operation(summary = "목표 상세 조회", description = "특정 목표의 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "목표를 찾을 수 없음")
    })
    public ResponseEntity<GoalDetailResponse> getGoalDetail(
            @Parameter(description = "목표 ID") @PathVariable Long goalId) {
        
        GoalDetailResponse response = goalService.getGoalDetail(goalId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{goalId}")
    @Operation(summary = "목표 수정", description = "목표 정보를 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "목표를 찾을 수 없음")
    })
    public ResponseEntity<GoalResponse> updateGoal(
            @Parameter(description = "목표 ID") @PathVariable Long goalId,
            @Valid @RequestBody UpdateGoalRequest request) {
        
        GoalResponse response = goalService.updateGoal(goalId, request);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{goalId}/progress")
    @Operation(summary = "목표 진행 상황 업데이트", description = "목표의 현재 진행 상황을 업데이트합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "업데이트 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "목표를 찾을 수 없음")
    })
    public ResponseEntity<GoalProgressResponse> updateProgress(
            @Parameter(description = "목표 ID") @PathVariable Long goalId,
            @Valid @RequestBody UpdateProgressRequest request) {
        
        GoalProgressResponse response = goalService.updateProgress(goalId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{goalId}")
    @Operation(summary = "목표 삭제", description = "목표를 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "목표를 찾을 수 없음")
    })
    public ResponseEntity<Void> deleteGoal(
            @Parameter(description = "목표 ID") @PathVariable Long goalId) {
        
        goalService.deleteGoal(goalId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/books/{bookId}/achievements")
    @Operation(summary = "달성한 목표 목록", description = "완료된 목표 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<List<AchievementResponse>> getAchievements(
            @Parameter(description = "가계부 ID") @PathVariable Long bookId) {
        
        List<AchievementResponse> achievements = goalService.getAchievements(bookId);
        return ResponseEntity.ok(achievements);
    }
} 