package garabu.garabuServer;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 가라부 서버 메인 애플리케이션 클래스
 * 
 * 이 클래스는 Spring Boot 애플리케이션의 진입점입니다.
 * 가계부 관리 시스템의 메인 애플리케이션으로, 
 * 회원 관리, 가계부 기록, 카테고리 관리 등의 기능을 제공합니다.
 * 
 * @author yhj
 * @version 1.0
 * @since 2024
 */
@SpringBootApplication
@MapperScan("garabu.garabuServer.mapper")
@OpenAPIDefinition(
    info = @Info(
        title = "Garabu API",
        version = "1.0",
        description = "가계부 관리 시스템 API 문서",
        contact = @Contact(
            name = "HyungJoo Yoon",
            email = "ujk6073@gmail.com"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(
            url = "http://localhost:8080",
            description = "개발 서버"
        )
    }
)
public class GarabuServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GarabuServerApplication.class, args);
	}
}

