package garabu.garabuServer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("garabu.garabuServer.mapper")
public class GarabuServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GarabuServerApplication.class, args);
	}
}

