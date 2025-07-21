package garabu.garabuServer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import garabu.garabuServer.domain.NotiApp;
import garabu.garabuServer.repository.NotiAppRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    
    private final NotiAppRepository notiAppRepository;
    private final ObjectMapper objectMapper;
    
    @Bean
    @Transactional
    public CommandLineRunner initializeNotiApp() {
        return args -> {
            try {
                // NotiApp 초기화
                initializeGarabuApp();
                log.info("NotiApp 초기화 완료");
            } catch (Exception e) {
                log.error("NotiApp 초기화 중 오류 발생", e);
            }
        };
    }
    
    private void initializeGarabuApp() throws IOException {
        String appId = "garabu-app";
        
        // 이미 존재하는지 확인
        if (notiAppRepository.existsById(appId)) {
            log.info("NotiApp '{}' 이미 존재함", appId);
            return;
        }
        
        // Firebase 서비스 계정 JSON 읽기
        String serviceAccountJson = readFirebaseServiceAccount();
        
        if (serviceAccountJson == null || serviceAccountJson.isEmpty()) {
            log.error("Firebase 서비스 계정 설정을 찾을 수 없습니다. 다음 중 하나를 설정해주세요:");
            log.error("1. resources/firebase/garabu-firebase-service-account.json 파일 생성");
            log.error("2. FCM_SERVICE_ACCOUNT_JSON 환경변수 설정");
            log.error("3. GOOGLE_APPLICATION_CREDENTIALS 환경변수로 파일 경로 지정");
            throw new IllegalStateException("Firebase 서비스 계정 설정이 필요합니다");
        }
        
        // NotiApp 엔티티 생성
        NotiApp notiApp = NotiApp.builder()
                .appId(appId)
                .fcmHttpV1At("Y")
                .fcmApiKey("") // HTTP v1에서는 사용하지 않음
                .fcmV1Config(serviceAccountJson)
                .pushUseAt("Y")
                .smsUseAt("N")
                .webUseAt("N")
                .useAt("Y")
                .build();
        
        notiAppRepository.save(notiApp);
        log.info("NotiApp '{}' 생성 완료", appId);
    }
    
    private String readFirebaseServiceAccount() throws IOException {
        // 1. 먼저 클래스패스에서 파일 읽기 시도
        ClassPathResource resource = new ClassPathResource("firebase/garabu-firebase-service-account.json");
        if (resource.exists()) {
            log.info("Firebase 서비스 계정 파일을 클래스패스에서 읽었습니다");
            return new String(resource.getInputStream().readAllBytes());
        }
        
        // 2. FCM_SERVICE_ACCOUNT_JSON 환경변수에서 JSON 직접 읽기
        String fcmConfig = System.getenv("FCM_SERVICE_ACCOUNT_JSON");
        if (fcmConfig != null && !fcmConfig.isEmpty()) {
            log.info("Firebase 서비스 계정 설정을 FCM_SERVICE_ACCOUNT_JSON 환경변수에서 읽었습니다");
            return fcmConfig;
        }
        
        // 3. GOOGLE_APPLICATION_CREDENTIALS 환경변수로 지정된 파일 읽기
        String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (credentialsPath != null && !credentialsPath.isEmpty()) {
            try {
                java.nio.file.Path path = java.nio.file.Paths.get(credentialsPath);
                if (java.nio.file.Files.exists(path)) {
                    log.info("Firebase 서비스 계정 파일을 GOOGLE_APPLICATION_CREDENTIALS 경로에서 읽었습니다: {}", credentialsPath);
                    return new String(java.nio.file.Files.readAllBytes(path));
                }
            } catch (Exception e) {
                log.warn("GOOGLE_APPLICATION_CREDENTIALS 파일 읽기 실패: {}", e.getMessage());
            }
        }
        
        return null;
    }
}