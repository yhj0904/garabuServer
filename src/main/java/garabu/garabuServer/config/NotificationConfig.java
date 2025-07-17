package garabu.garabuServer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "notification")
public class NotificationConfig {
    
    private String provider = "fcm"; // default to FCM
    private ExpoConfig expo = new ExpoConfig();
    
    public String getProvider() {
        return provider;
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    public ExpoConfig getExpo() {
        return expo;
    }
    
    public void setExpo(ExpoConfig expo) {
        this.expo = expo;
    }
    
    public boolean isExpoEnabled() {
        return "expo".equalsIgnoreCase(provider) && expo.isEnabled();
    }
    
    public boolean isFcmEnabled() {
        return "fcm".equalsIgnoreCase(provider);
    }
    
    public static class ExpoConfig {
        private boolean enabled = false;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}