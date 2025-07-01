package garabu.garabuServer.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
public class LogTestController {

    @GetMapping("/log")
    public String logTest() {
        log.info("ELK 로그 테스트 - {}", LocalDateTime.now());
        return "로그 전송 완료";
    }
}
