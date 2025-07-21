package garabu.garabuServer.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("Firebase 이미 초기화됨");
            return;
        }

        GoogleCredentials credentials = getCredentials();
        
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();

        FirebaseApp.initializeApp(options);
        log.info("Firebase Admin SDK 초기화 완료");
    }
    
    private GoogleCredentials getCredentials() throws IOException {
        // 1. Application Default Credentials (ADC) 시도 - 가장 권장되는 방법
        try {
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            log.info("Application Default Credentials (ADC)를 사용하여 Firebase 초기화");
            return credentials;
        } catch (IOException e) {
            log.debug("ADC를 찾을 수 없음, 다른 방법 시도: {}", e.getMessage());
        }
        
        // 2. 클래스패스에서 서비스 계정 파일 찾기
        String[] possiblePaths = {
            "firebase/garabu-firebase-service-account.json",
            "firebase/firebase-service-account.json",
            "garabu-firebase-service-account.json",
            "firebase-service-account.json"
        };
        
        for (String path : possiblePaths) {
            ClassPathResource resource = new ClassPathResource(path);
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    log.info("클래스패스에서 Firebase 서비스 계정 파일 사용: {}", path);
                    return GoogleCredentials.fromStream(is);
                }
            }
        }
        
        // 3. GOOGLE_APPLICATION_CREDENTIALS 환경변수로 지정된 파일 사용
        String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (credentialsPath != null && !credentialsPath.isEmpty()) {
            try (FileInputStream serviceAccount = new FileInputStream(credentialsPath)) {
                log.info("GOOGLE_APPLICATION_CREDENTIALS 환경변수의 파일 사용: {}", credentialsPath);
                return GoogleCredentials.fromStream(serviceAccount);
            } catch (IOException e) {
                log.warn("GOOGLE_APPLICATION_CREDENTIALS 파일을 읽을 수 없음: {}", e.getMessage());
            }
        }
        
        throw new IllegalStateException(
            "Firebase 인증 정보를 찾을 수 없습니다. 다음 중 하나를 설정해주세요:\n" +
            "1. Google Cloud 환경에서 실행 (App Engine, Cloud Run, GKE 등)\n" +
            "2. GOOGLE_APPLICATION_CREDENTIALS 환경변수 설정\n" +
            "3. gcloud auth application-default login 실행 (로컬 개발용)\n" +
            "4. resources/firebase/ 디렉토리에 서비스 계정 JSON 파일 배치"
        );
    }
}