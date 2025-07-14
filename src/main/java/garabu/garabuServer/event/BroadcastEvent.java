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
public class BroadcastEvent implements Serializable {
    private String eventType;
    private Object data;
    private Long timestamp;
} 