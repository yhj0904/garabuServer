package garabu.garabuServer.api;

import garabu.garabuServer.domain.AmountType;
import garabu.garabuServer.domain.Category;
import garabu.garabuServer.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CategoryApiController {
    private final CategoryService categoryService;

    @PostMapping("/api/v2/category")
    public CreateCategoryResponse categoryV2(@RequestBody @Valid
                                                 CreateCategoryRequest request){
        Category category = new Category();
        category.setCategoryName(request.getCategoryName());
        category.setAmountType(request.getAmountType());
        Long id = categoryService.rigistCategory(category);
        return new CreateCategoryResponse(id);
    }

    @Data
    static class CreateCategoryRequest {
        @Email
        private String categoryName;
        private AmountType amountType;

    }
    @Data
    static class CreateCategoryResponse {
        private Long id;
        public CreateCategoryResponse(Long id) {
            this.id = id;
        }
    }
}
