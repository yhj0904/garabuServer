package garabu.garabuServer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {

        corsRegistry.addMapping("/**")
                .exposedHeaders("Set-Cookie", "access", "refresh", "Access-Control-Expose-Headers")
                .allowedOrigins("http://localhost:5173",
                                "http://localhost:4000",
                                "http://192.168.10.54:8081",
                                "http://101.1.13.107:8081")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}