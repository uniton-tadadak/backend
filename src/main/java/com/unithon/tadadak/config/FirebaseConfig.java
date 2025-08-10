package com.unithon.tadadak.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                // 개발 환경에서는 환경 변수로 서비스 키 파일 경로 설정
                // 실제 배포 시에는 serviceAccountKey.json 파일을 resources에 추가하거나
                // 환경 변수로 JSON 내용을 직접 설정
                InputStream serviceAccount = new ClassPathResource("firebase-service-account.json").getInputStream();
                
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK 초기화 완료");
            }
        } catch (IOException e) {
            // 개발 환경에서 Firebase 설정 파일이 없을 경우 경고만 출력
            log.warn("Firebase 설정 파일을 찾을 수 없습니다. 채팅 기능이 비활성화됩니다: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Firebase 초기화 실패", e);
        }
    }

    @Bean
    @ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = false)
    public Firestore firestore() {
        try {
            return FirestoreClient.getFirestore();
        } catch (Exception e) {
            log.error("Firestore 클라이언트 생성 실패", e);
            throw new RuntimeException("Firebase 설정이 올바르지 않습니다.", e);
        }
    }
} 