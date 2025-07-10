package garabu.garabuServer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {

        corsRegistry.addMapping("/**")
                .exposedHeaders("Set-Cookie", "access")
                .allowedOrigins("http://localhost:5173",
                                "http://localhost:4000",
                                "http://192.168.10.54:8081",
                                "http://14.5.176.24:19000",  // Expo Go 디버거
                                "http://14.5.176.24:19006");
    }
}