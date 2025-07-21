package garabu.garabuServer.dto.tag;

import lombok.Data;

import java.util.List;

@Data
public class TagSearchResponse {
    private String tagName;
    private String tagColor;
    private List<TransactionSummary> transactions;
    private Long totalElements;
    private Integer totalPages;
    private Integer currentPage;
} 