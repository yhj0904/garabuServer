package garabu.garabuServer.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "expo_user_token", indexes = {
    @Index(name = "idx_user_device", columnList = "user_id, device_id"),
    @Index(name = "idx_expo_token", columnList = "expo_token")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpoUserToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "device_id", nullable = false, length = 100)
    private String deviceId;
    
    @Column(name = "expo_token", nullable = false, length = 200, unique = true)
    private String expoToken;
    
    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "active", nullable = false)
    private boolean active = true;
    
    @Column(name = "platform", length = 20)
    private String platform; // ios, android
    
    @Column(name = "app_version", length = 20)
    private String appVersion;
}