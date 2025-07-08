package garabu.garabuServer.api;

import garabu.garabuServer.domain.Category;
import garabu.garabuServer.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 카테고리(Category) 관리 REST 컨트롤러
 *
 * <p>수입·지출·이체 등 가계부 항목을 분류하는 카테고리 생성·조회 기능을 제공합니다.
 * 모든 엔드포인트는 JWT Bearer 토큰 인증이 필요합니다.</p>
 *
 * @author yhj
 * @version 2.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/category")      // 공통 prefix
@Tag(name = "Category", description = "카테고리 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class CategoryApiController {

    private final CategoryService categoryService;

    // ───────────────────────── 카테고리 생성 ─────────────────────────
    /**
     * 새로운 카테고리를 생성합니다.
     *
     * @param request 카테고리 생성 요청 DTO
     * @return 생성된 카테고리 ID
     */
    @PostMapping
    @Operation(
            summary     = "카테고리 생성",
            description = "새로운 카테고리(Category)를 생성하고 고유 ID를 반환합니다."
    )
    @RequestBody(
            required = true,
            description = "카테고리 생성 요청 본문",
            content = @Content(
                    schema = @Schema(implementation = CreateCategoryRequest.class),
                    examples = @ExampleObject(
                            name  = "예시",
                            value = "{ \"category\": \"급여\" }"
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description  = "카테고리 생성 성공",
                    content      = @Content(schema = @Schema(implementation = CreateCategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<CreateCategoryResponse> createCategory(
            @Valid @org.springframework.web.bind.annotation.RequestBody CreateCategoryRequest request) {

        Category category = new Category();
        category.setCategory(request.getCategory());

        Long id = categoryService.rigistCategory(category);
        return ResponseEntity
                .status(201)
                .body(new CreateCategoryResponse(id));
    }

    // ───────────────────────── 카테고리 목록 조회 ─────────────────────────
    /**
     * 시스템에 등록된 카테고리 목록을 조회합니다.
     *
     * @return 카테고리 리스트
     */
    @GetMapping("/list")
    @Operation(
            summary     = "카테고리 목록 조회",
            description = "등록된 모든 카테고리(Category)를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description  = "카테고리 목록 조회 성공",
                    content      = @Content(schema = @Schema(implementation = ListCategoryResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<ListCategoryResponse> listCategories() {
        List<Category> categories = categoryService.findAllCategories();

        List<ListCategoryDto> data = categories.stream()
                .map(c -> new ListCategoryDto(c.getId(), c.getCategory()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ListCategoryResponse(data));
    }

    // ───────────────────────── DTO 정의 ─────────────────────────
    /** 카테고리 생성 요청 DTO */
    @Data
    @Schema(description = "카테고리 생성 요청 DTO")
    static class CreateCategoryRequest {
        @Schema(description = "카테고리명", example = "급여", requiredMode = Schema.RequiredMode.REQUIRED)
        private String category;
    }

    /** 카테고리 생성 응답 DTO */
    @Data
    @Schema(description = "카테고리 생성 응답 DTO")
    static class CreateCategoryResponse {
        @Schema(description = "생성된 카테고리 ID", example = "7")
        private Long id;

        public CreateCategoryResponse(Long id) {
            this.id = id;
        }
    }

    /** 카테고리 목록 응답 DTO */
    @Data
    @Schema(description = "카테고리 목록 응답 DTO")
    static class ListCategoryResponse {
        @Schema(description = "카테고리 배열")
        private List<ListCategoryDto> categories;

        public ListCategoryResponse(List<ListCategoryDto> categories) {
            this.categories = categories;
        }
    }

    /** 카테고리 단건 DTO */
    @Data
    @Schema(description = "카테고리 단건 DTO")
    static class ListCategoryDto {
        @Schema(description = "카테고리 ID", example = "7")
        private Long id;

        @Schema(description = "카테고리명", example = "식비")
        private String category;

        public ListCategoryDto(Long id, String category) {
            this.id = id;
            this.category = category;
        }
    }
}
