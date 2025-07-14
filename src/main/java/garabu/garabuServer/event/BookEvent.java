package garabu.garabuServer.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookEvent implements Serializable {
    private Long bookId;
    private String eventType;
    private Object data;
    private Long userId;
    private Long timestamp;
    
    public static BookEvent ledgerCreated(Long bookId, Long userId, Object ledgerData) {
        return BookEvent.builder()
                .bookId(bookId)
                .eventType("LEDGER_CREATED")
                .data(ledgerData)
                .userId(userId)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    public static BookEvent ledgerUpdated(Long bookId, Long userId, Object ledgerData) {
        return BookEvent.builder()
                .bookId(bookId)
                .eventType("LEDGER_UPDATED")
                .data(ledgerData)
                .userId(userId)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    public static BookEvent ledgerDeleted(Long bookId, Long userId, Long ledgerId) {
        return BookEvent.builder()
                .bookId(bookId)
                .eventType("LEDGER_DELETED")
                .data(ledgerId)
                .userId(userId)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    public static BookEvent memberAdded(Long bookId, Long userId, Object memberData) {
        return BookEvent.builder()
                .bookId(bookId)
                .eventType("MEMBER_ADDED")
                .data(memberData)
                .userId(userId)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    public static BookEvent memberRemoved(Long bookId, Long userId, Long removedUserId) {
        return BookEvent.builder()
                .bookId(bookId)
                .eventType("MEMBER_REMOVED")
                .data(removedUserId)
                .userId(userId)
                .timestamp(System.currentTimeMillis())
                .build();
    }
} 