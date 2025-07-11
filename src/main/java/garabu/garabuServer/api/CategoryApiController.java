package garabu.garabuServer.api;

import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.Category;
import garabu.garabuServer.service.BookService;
import garabu.garabuServer.service.CategoryService;
import garabu.garabuServer.service.MemberService;
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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    private static final Logger logger = LoggerFactory.getLogger(CategoryApiController.class);
    private final CategoryService categoryService;
    private final BookService bookService;
    private final MemberService memberService;

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

        logger.info("카테고리 생성 요청: {}", request.getCategory());

        // 중복 검사
        Category existingCategory = categoryService.findByCategory(request.getCategory());
        if (existingCategory != null) {
            logger.warn("중복된 카테고리명: {}", request.getCategory());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 존재하는 카테고리명입니다.");
        }

        Category category = new Category();
        category.setCategory(request.getCategory());

        Long id = categoryService.rigistCategory(category);
        logger.info("카테고리 생성 완료 - ID: {}", id);

        return ResponseEntity
                .status(201)
                .body(new CreateCategoryResponse(id, request.getCategory()));
    }

    // ───────────────────────── 카테고리 목록 조회 ─────────────────────────
    /**
     * 등록된 모든 카테고리를 조회합니다.
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
                .map(c -> new ListCategoryDto(c.getId(), c.getCategory(), c.getEmoji(), c.getIsDefault()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ListCategoryResponse(data));
    }

    // ───────────────────────── 가계부별 카테고리 목록 조회 (기본 + 사용자 정의) ─────────────────────────
    /**
     * 특정 가계부의 카테고리 목록을 조회합니다.
     * 기본 제공 카테고리 + 해당 가계부의 사용자 정의 카테고리를 모두 반환합니다.
     *
     * @param bookId 가계부 ID
     * @return 가계부별 카테고리 리스트 (기본 + 사용자 정의)
     */
    @GetMapping("/book/{bookId}")
    @Operation(
            summary     = "가계부별 카테고리 목록 조회",
            description = "특정 가계부의 기본 제공 카테고리 + 사용자 정의 카테고리를 모두 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description  = "가계부별 카테고리 목록 조회 성공",
                    content      = @Content(schema = @Schema(implementation = ListCategoryResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "가계부 접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<ListCategoryResponse> listCategoriesByBook(@PathVariable Long bookId) {
        Book book = bookService.findById(bookId);
        
        // 사용자가 해당 가계부에 접근 권한이 있는지 확인
        categoryService.validateBookAccess(book);
        
        // 기본 카테고리 + 가계부별 사용자 정의 카테고리 조회
        List<Category> categories = categoryService.findCombinedCategories(book);

        List<ListCategoryDto> data = categories.stream()
                .map(c -> new ListCategoryDto(c.getId(), c.getCategory(), c.getEmoji(), c.getIsDefault()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ListCategoryResponse(data));
    }
    
    // ───────────────────────── 기본 제공 카테고리 목록 조회 ─────────────────────────
    /**
     * 기본 제공 카테고리 목록을 조회합니다.
     *
     * @return 기본 제공 카테고리 리스트
     */
    @GetMapping("/default")
    @Operation(
            summary     = "기본 제공 카테고리 목록 조회",
            description = "모든 사용자에게 공통으로 제공되는 기본 카테고리 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description  = "기본 카테고리 목록 조회 성공",
                    content      = @Content(schema = @Schema(implementation = ListCategoryResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<ListCategoryResponse> listDefaultCategories() {
        List<Category> categories = categoryService.findDefaultCategories();

        List<ListCategoryDto> data = categories.stream()
                .map(c -> new ListCategoryDto(c.getId(), c.getCategory(), c.getEmoji(), c.getIsDefault()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ListCategoryResponse(data));
    }

    // ───────────────────────── 가계부별 카테고리 생성 ─────────────────────────
    /**
     * 특정 가계부에 새로운 카테고리를 생성합니다.
     *
     * @param bookId 가계부 ID
     * @param request 카테고리 생성 요청 DTO
     * @return 생성된 카테고리 ID
     */
    @PostMapping("/book/{bookId}")
    @Operation(
            summary     = "가계부별 사용자 정의 카테고리 생성",
            description = "특정 가계부에 새로운 사용자 정의 카테고리를 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description  = "가계부별 카테고리 생성 성공",
                    content      = @Content(schema = @Schema(implementation = CreateCategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "가계부 수정 권한 없음"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<CreateCategoryResponse> createCategoryForBook(
            @PathVariable Long bookId,
            @Valid @org.springframework.web.bind.annotation.RequestBody CreateCategoryRequest request) {

        logger.info("가계부별 카테고리 생성 요청 - 가계부ID: {}, 카테고리: {}", bookId, request.getCategory());

        Book book = bookService.findById(bookId);
        
        // 가계부 수정 권한 확인
        categoryService.validateBookEditAccess(book);
        
        // 가계부 내 중복 검사 (기본 카테고리 + 사용자 정의 카테고리)
        List<Category> existingCategories = categoryService.findCombinedCategories(book);
        boolean isDuplicate = existingCategories.stream()
                .anyMatch(c -> c.getCategory().equals(request.getCategory()));
        
        if (isDuplicate) {
            logger.warn("가계부 내 중복된 카테고리명: {}", request.getCategory());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 존재하는 카테고리명입니다.");
        }

        Long id = categoryService.createCategoryForBook(book, request.getCategory());
        logger.info("가계부별 카테고리 생성 완료 - ID: {}", id);

        return ResponseEntity
                .status(201)
                .body(new CreateCategoryResponse(id, request.getCategory()));
    }

    // ───────────────────────── DTO 정의 ─────────────────────────
    /** 카테고리 생성 요청 DTO */
    @Data
    @Schema(description = "카테고리 생성 요청 DTO")
    static class CreateCategoryRequest {
        @NotBlank(message = "카테고리명은 필수입니다")
        @Size(min = 1, max = 20, message = "카테고리명은 1자 이상 20자 이하여야 합니다")
        @Schema(description = "카테고리명", example = "급여", requiredMode = Schema.RequiredMode.REQUIRED)
        private String category;
    }

    /** 카테고리 생성 응답 DTO */
    @Data
    @Schema(description = "카테고리 생성 응답 DTO")
    static class CreateCategoryResponse {
        @Schema(description = "생성된 카테고리 ID", example = "7")
        private Long id;
        
        @Schema(description = "카테고리명", example = "급여")
        private String category;

        public CreateCategoryResponse(Long id, String category) {
            this.id = id;
            this.category = category;
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
        
        @Schema(description = "카테고리 이모지", example = "🍽️")
        private String emoji;
        
        @Schema(description = "기본 제공 카테고리 여부", example = "true")
        private Boolean isDefault;

        public ListCategoryDto(Long id, String category, String emoji, Boolean isDefault) {
            this.id = id;
            this.category = category;
            this.emoji = emoji;
            this.isDefault = isDefault;
        }
    }
}
