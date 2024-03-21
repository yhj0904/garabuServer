package garabu.garabuServer.api;

import garabu.garabuServer.domain.AmountType;
import garabu.garabuServer.domain.Category;
import garabu.garabuServer.domain.UserBook;
import garabu.garabuServer.service.UserBookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserBookApiController {
    private final UserBookService userBookService;



    @Data
    static class CreateUserBookRequest {
        @Email
        private String categoryName;
        private AmountType amountType;

    }
    @Data
    static class CreateUserBookResponse {
        private Long id;
        public CreateUserBookResponse(Long id) {
            this.id = id;
        }
    }
}
