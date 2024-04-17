package garabu.garabuServer.api;

import garabu.garabuServer.domain.AmountType;
import garabu.garabuServer.domain.Category;
import garabu.garabuServer.service.CategoryService;
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

@RestController
@RequiredArgsConstructor
public class CategoryApiController {
    private final CategoryService categoryService;

    @PostMapping("/api/v2/category")
    public CreateCategoryResponse categoryV2(@RequestBody @Valid
                                                 CreateCategoryRequest request){
        Category category = new Category();
        category.setCategory(request.getCategory());
        Long id = categoryService.rigistCategory(category);
        return new CreateCategoryResponse(id);
    }

    @GetMapping("/api/v2/category/list")
    public ListCategoryResponse listCategories() {
        List<Category> categories = categoryService.findAllCategories();
        List<ListCategoryDto> data = categories.stream()
                .map(c -> new ListCategoryDto(c.getId(), c.getCategory()))
                .collect(Collectors.toList());
        return new ListCategoryResponse(data);
    }

    @Data
    static class ListCategoryResponse {
        private List<ListCategoryDto> categories;
        public ListCategoryResponse(List<ListCategoryDto> categories) {
            this.categories = categories;
        }
    }

    @Data
    static class ListCategoryDto {
        private Long id;
        private String category;
        public ListCategoryDto(Long id, String category) {
            this.id = id;
            this.category = category;
        }
    }

    @Data
    static class CreateCategoryRequest {
        private String category;
    }
    @Data
    static class CreateCategoryResponse {
        private Long id;
        public CreateCategoryResponse(Long id) {
            this.id = id;
        }
    }
}
