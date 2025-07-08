package garabu.garabuServer.api;

import garabu.garabuServer.domain.AmountType;
import garabu.garabuServer.domain.Category;
import garabu.garabuServer.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 카테고리 관리 API 컨트롤러
 * 
 * 가계부 기록의 카테고리 생성, 조회 등의 기능을 제공합니다.
 * 수입, 지출, 이체 등의 분류를 위한 카테고리를 관리합니다.
 * 
 * @author Garabu Team
 * @version 1.0
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Category", description = "카테고리 관리 API")
public class CategoryApiController {
    
    private final CategoryService categoryService;

    /**
     * 새로운 카테고리를 생성합니다.
     * 
     * @param request 카테고리 생성 요청 정보
     * @return 생성된 카테고리의 ID
     */
    @PostMapping("/api/v2/category")
    @Operation(
        summary = "카테고리 생성",
        description = "새로운 카테고리를 생성합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "카테고리 생성 성공",
            content = @Content(schema = @Schema(implementation = CreateCategoryResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류"
        )
    })
    public CreateCategoryResponse categoryV2(
        @Parameter(description = "카테고리 생성 정보", required = true)
        @RequestBody @Valid CreateCategoryRequest request){
        
        Category category = new Category();
        category.setCategory(request.getCategory());
        Long id = categoryService.rigistCategory(category);
        return new CreateCategoryResponse(id);
    }

    /**
     * 모든 카테고리 목록을 조회합니다.
     * 
     * @return 카테고리 목록
     */
    @GetMapping("/api/v2/category/list")
    @Operation(
        summary = "카테고리 목록 조회",
        description = "시스템에 등록된 모든 카테고리를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "카테고리 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = ListCategoryResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류"
        )
    })
    public ListCategoryResponse listCategories() {
        List<Category> categories = categoryService.findAllCategories();
        List<ListCategoryDto> data = categories.stream()
                .map(c -> new ListCategoryDto(c.getId(), c.getCategory()))
                .collect(Collectors.toList());
        return new ListCategoryResponse(data);
    }

    /**
     * 카테고리 목록 응답 DTO
     */
    @Data
    static class ListCategoryResponse {
        @Parameter(description = "카테고리 목록")
        private List<ListCategoryDto> categories;
        
        public ListCategoryResponse(List<ListCategoryDto> categories) {
            this.categories = categories;
        }
    }

    /**
     * 카테고리 정보 DTO
     */
    @Data
    static class ListCategoryDto {
        @Parameter(description = "카테고리 ID")
        private Long id;
        
        @Parameter(description = "카테고리명")
        private String category;
        
        public ListCategoryDto(Long id, String category) {
            this.id = id;
            this.category = category;
        }
    }

    /**
     * 카테고리 생성 요청 DTO
     */
    @Data
    static class CreateCategoryRequest {
        @Parameter(description = "카테고리명", example = "급여", required = true)
        private String category;
    }
    
    /**
     * 카테고리 생성 응답 DTO
     */
    @Data
    static class CreateCategoryResponse {
        @Parameter(description = "생성된 카테고리의 ID")
        private Long id;
        
        public CreateCategoryResponse(Long id) {
            this.id = id;
        }
    }
}
