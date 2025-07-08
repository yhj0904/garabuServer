package garabu.garabuServer.api;

import garabu.garabuServer.domain.UserBook;
import garabu.garabuServer.service.UserBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 가계부(UserBook) 소유자 조회 REST 컨트롤러
 *
 * <p>bookId를 받아 해당 가계부를 소유한 회원(Owner) 목록을 반환합니다.
 * 모든 요청은 JWT Bearer 토큰 인증이 필요합니다.</p>
 *
 * @author yhj
 * @version 1.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/book")            // ⬅️ book 자원 하위로 위치
@Tag(name = "UserBook", description = "가계부 소유자 조회 API")
@SecurityRequirement(name = "bearerAuth")  // Swagger UI Authorize 버튼
public class UserBookApiController {

    private final UserBookService userBookService;

    /* ───────────────────────── 가계부 소유자 목록 조회 ───────────────────────── */
    /**
     * bookId에 해당하는 가계부를 소유한 사용자 목록을 조회합니다.
     *
     * @param bookId 조회할 가계부 ID
     * @return Owner 리스트
     */
    @GetMapping("/{bookId}/owners")
    @Operation(
            summary     = "가계부 소유자 목록 조회",
            description = "bookId에 해당하는 가계부를 소유(공유)하고 있는 모든 회원 정보를 반환합니다."
    )
    @Parameter(name = "bookId", description = "가계부 ID", required = true, example = "42")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description  = "소유자 목록 조회 성공",
                    content      = @Content(schema = @Schema(implementation = ListOwnerResponse.class))),
            @ApiResponse(responseCode = "404", description = "해당 ID의 가계부 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<ListOwnerResponse> listOwnersByBookId(@PathVariable Long bookId) {

        /* 1) 서비스 호출 → UserBook 엔티티 목록 */
        List<UserBook> userBooks = userBookService.findOwnersByBookId(bookId);

        /* 2) DTO 변환 (회원 ID·이름·이메일만 노출) */
        List<OwnerDto> owners = userBooks.stream()
                .map(ub -> new OwnerDto(
                        ub.getMember().getId(),
                        ub.getMember().getUsername(),
                        ub.getMember().getEmail()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ListOwnerResponse(owners));
    }

    /* ───────────────────────── DTO 정의 ───────────────────────── */
    /** 가계부 소유자(회원) 요약 DTO */
    @Data
    @Schema(description = "가계부 소유자(회원) DTO")
    static class OwnerDto {
        @Schema(description = "회원 ID", example = "11")
        private Long memberId;

        @Schema(description = "회원 이름", example = "홍길동")
        private String username;

        @Schema(description = "회원 이메일", example = "user@example.com")
        private String email;

        public OwnerDto(Long memberId, String username, String email) {
            this.memberId = memberId;
            this.username = username;
            this.email = email;
        }
    }

    /** 가계부 소유자 목록 응답 DTO (래퍼) */
    @Data
    @Schema(description = "가계부 소유자 목록 응답 DTO")
    static class ListOwnerResponse {
        @Schema(description = "소유자 배열")
        private List<OwnerDto> owners;

        public ListOwnerResponse(List<OwnerDto> owners) {
            this.owners = owners;
        }
    }
}
