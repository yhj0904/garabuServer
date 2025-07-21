package garabu.garabuServer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * 가라부(Garabu) 서버 애플리케이션의 메인 클래스
 * 
 * 이 애플리케이션은 가계부 관리 시스템의 백엔드 서버입니다.
 * Spring Boot를 기반으로 REST API를 제공하며, 
 * MyBatis를 사용하여 데이터베이스와 통신합니다.
 * 
 * @author Garabu Team
 * @version 1.0
 * @since 2024-01-01
 */
@SpringBootApplication
@MapperScan("garabu.garabuServer.mapper")
@EnableScheduling
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

