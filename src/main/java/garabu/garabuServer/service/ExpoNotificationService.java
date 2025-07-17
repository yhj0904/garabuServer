package garabu.garabuServer.service;

import com.niamedtech.expo.exposerversdk.ExpoPushNotificationClient;
import com.niamedtech.expo.exposerversdk.request.PushNotification;
import com.niamedtech.expo.exposerversdk.response.TicketResponse;
import com.niamedtech.expo.exposerversdk.response.ReceiptResponse;
import com.niamedtech.expo.exposerversdk.response.Status;
import com.niamedtech.expo.exposerversdk.exception.ErrorResponseException;
import com.niamedtech.expo.exposerversdk.util.PushNotificationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExpoNotificationService {

    private final ExpoPushNotificationClient expoPushClient;

    public ExpoNotificationService() {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        this.expoPushClient = ExpoPushNotificationClient.builder()
            .setHttpClient(httpClient)
            .build();
    }

    public CompletableFuture<List<TicketResponse.Ticket>> sendPushNotification(String token, String title, String body, Map<String, Object> data) {
        if (!PushNotificationUtil.isExponentPushToken(token)) {
            log.error("Invalid Expo push token: {}", token);
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        PushNotification message = new PushNotification();
        message.setTo(Arrays.asList(token));
        message.setTitle(title);
        message.setBody(body);
        message.setSound("default");
        message.setPriority(PushNotification.Priority.NORMAL);
        
        if (data != null && !data.isEmpty()) {
            message.setData(data);
        }

        List<PushNotification> messages = new ArrayList<>();
        messages.add(message);

        try {
            List<TicketResponse.Ticket> tickets = expoPushClient.sendPushNotifications(messages);
            
            // Log any errors
            for (int i = 0; i < tickets.size(); i++) {
                TicketResponse.Ticket ticket = tickets.get(i);
                if (ticket.getStatus() == Status.ERROR) {
                    log.error("Push notification failed: {} - {}", 
                        ticket.getMessage(), ticket.getDetails());
                }
            }
            
            return CompletableFuture.completedFuture(tickets);
        } catch (Exception e) {
            log.error("Failed to send Expo push notification", e);
            return CompletableFuture.completedFuture(new ArrayList<>());
        }
    }

    public CompletableFuture<List<TicketResponse.Ticket>> sendPushNotifications(List<String> tokens, String title, String body, Map<String, Object> data) {
        // Filter out invalid tokens
        List<String> validTokens = tokens.stream()
                .filter(PushNotificationUtil::isExponentPushToken)
                .collect(Collectors.toList());

        if (validTokens.isEmpty()) {
            log.warn("No valid Expo push tokens found");
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        // Create messages in chunks (Expo recommends max 100 per request)
        List<PushNotification> messages = new ArrayList<>();
        for (String token : validTokens) {
            PushNotification message = new PushNotification();
            message.setTo(Arrays.asList(token));
            message.setTitle(title);
            message.setBody(body);
            message.setSound("default");
            message.setPriority(PushNotification.Priority.NORMAL);
            
            if (data != null && !data.isEmpty()) {
                message.setData(data);
            }
            
            messages.add(message);
        }

        // Send in chunks of 100
        List<CompletableFuture<List<TicketResponse.Ticket>>> futures = new ArrayList<>();
        for (int i = 0; i < messages.size(); i += 100) {
            List<PushNotification> chunk = messages.subList(i, Math.min(i + 100, messages.size()));
            
            try {
                CompletableFuture<List<TicketResponse.Ticket>> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        return expoPushClient.sendPushNotifications(chunk);
                    } catch (Exception e) {
                        log.error("Failed to send push notification chunk", e);
                        return new ArrayList<TicketResponse.Ticket>();
                    }
                });
                
                CompletableFuture<List<TicketResponse.Ticket>> ticketsFuture = future.thenApply(tickets -> {
                    // Log any errors
                    for (TicketResponse.Ticket ticket : tickets) {
                        if (ticket.getStatus() == Status.ERROR) {
                            log.error("Push notification failed: {} - {}", 
                                ticket.getMessage(), ticket.getDetails());
                        }
                    }
                    
                    return tickets;
                });
                
                futures.add(ticketsFuture);
            } catch (Exception e) {
                log.error("Failed to send chunk of push notifications", e);
            }
        }

        // Combine all results
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList()));
    }

    public void checkReceipts(List<String> receiptIds) {
        if (receiptIds == null || receiptIds.isEmpty()) {
            return;
        }

        // Filter out null receipt IDs
        List<String> validReceiptIds = receiptIds.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if (validReceiptIds.isEmpty()) {
            return;
        }

        try {
            CompletableFuture.supplyAsync(() -> {
                try {
                    return expoPushClient.getPushNotificationReceipts(validReceiptIds);
                } catch (Exception e) {
                    log.error("Failed to get push receipts", e);
                    return null;
                }
            }).thenAccept(receipts -> {
                if (receipts == null || receipts.isEmpty()) {
                    return;
                }
                
                for (String receiptId : validReceiptIds) {
                    ReceiptResponse.Receipt receipt = receipts.get(receiptId);
                    if (receipt == null) {
                        log.warn("No receipt found for ID: {}", receiptId);
                        continue;
                    }
                    
                    if (receipt.getStatus() == Status.ERROR) {
                        log.error("Push notification failed for receipt {}: {}", 
                            receiptId, receipt.getMessage());
                        
                        // Handle specific errors
                        if (receipt.getDetails() != null && 
                            "DeviceNotRegistered".equals(receipt.getDetails().getError())) {
                            // TODO: Remove invalid token from database
                            log.info("Device not registered, should remove token");
                        }
                    } else if (receipt.getStatus() == Status.OK) {
                        log.debug("Push notification delivered successfully for receipt {}", receiptId);
                    }
                }
            }).exceptionally(e -> {
                log.error("Failed to check push receipts", e);
                return null;
            });
        } catch (Exception e) {
            log.error("Error checking push receipts", e);
        }
    }
}