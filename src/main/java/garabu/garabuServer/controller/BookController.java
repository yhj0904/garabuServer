package garabu.garabuServer.controller;

import garabu.garabuServer.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class BookController {

    @GetMapping("/book/regist")
    public String creatBook(){
        return "write";
    }
}
