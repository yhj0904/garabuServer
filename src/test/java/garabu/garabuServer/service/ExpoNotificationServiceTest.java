package garabu.garabuServer.service;

import org.junit.jupiter.api.Test;
import com.niamedtech.expo.exposerversdk.util.PushNotificationUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExpoNotificationServiceTest {

    @Test
    void testServiceCreation() {
        ExpoNotificationService service = new ExpoNotificationService();
        assertNotNull(service);
    }

    @Test
    void testInvalidTokenHandling() {
        ExpoNotificationService service = new ExpoNotificationService();
        
        // Test with invalid token
        String invalidToken = "invalid-token";
        Map<String, Object> data = new HashMap<>();
        data.put("test", "data");
        
        // This should complete without throwing an exception
        assertDoesNotThrow(() -> {
            service.sendPushNotification(invalidToken, "Test Title", "Test Body", data);
        });
    }

    @Test 
    void testValidExpoTokenValidation() {
        // Test Expo token validation utility
        String validToken = "ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]";
        String invalidToken = "invalid-token";
        
        assertTrue(PushNotificationUtil.isExponentPushToken(validToken));
        assertFalse(PushNotificationUtil.isExponentPushToken(invalidToken));
    }
}