package garabu.garabuServer.dto.tag;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TagResponse {
    private Long id;
    private String name;
    private String color;
    private Integer usageCount;
    private LocalDateTime createdAt;
} 