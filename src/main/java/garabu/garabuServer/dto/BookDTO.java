package garabu.garabuServer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.BookRole;
import garabu.garabuServer.domain.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 가계부 정보 전송 객체
 * Redis 캐싱과 JSON 직렬화를 위한 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "가계부 정보 DTO")
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "가계부 ID", example = "1")
    private Long id;
    
    @Schema(description = "가계부 이름", example = "개인 가계부")
    private String title;
    
    @Schema(description = "소유자 정보")
    private OwnerDTO owner;
    
    @Schema(description = "참여자 수", example = "3")
    private Integer memberCount;
    
    @Schema(description = "현재 사용자의 권한", example = "OWNER")
    private BookRole currentUserRole;
    
    /**
     * 소유자 정보 DTO (간단한 정보만 포함)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "소유자 정보")
    public static class OwnerDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @Schema(description = "소유자 ID", example = "1")
        private Long id;
        
        @Schema(description = "소유자 이름", example = "홍길동")
        private String name;
        
        @Schema(description = "소유자 이메일", example = "user@example.com")
        private String email;
        
        /**
         * Member 엔티티를 OwnerDTO로 변환
         */
        public static OwnerDTO from(Member member) {
            if (member == null) return null;
            
            return OwnerDTO.builder()
                    .id(member.getId())
                    .name(member.getName())
                    .email(member.getEmail())
                    .build();
        }
    }
    
    /**
     * Book 엔티티를 BookDTO로 변환
     * 
     * @param book Book 엔티티
     * @param currentUserRole 현재 사용자의 권한 (옵션)
     * @return BookDTO
     */
    public static BookDTO from(Book book, BookRole currentUserRole) {
        if (book == null) return null;
        
        return BookDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .owner(OwnerDTO.from(book.getOwner()))
                .memberCount(book.getUserBooks() != null ? book.getUserBooks().size() : 0)
                .currentUserRole(currentUserRole)
                .build();
    }
    
    /**
     * Book 엔티티를 BookDTO로 변환 (권한 정보 없이)
     */
    public static BookDTO from(Book book) {
        return from(book, null);
    }
    
    /**
     * Book 엔티티 리스트를 BookDTO 리스트로 변환
     */
    public static List<BookDTO> from(List<Book> books) {
        if (books == null) return List.of();
        
        return books.stream()
                .map(BookDTO::from)
                .collect(Collectors.toList());
    }
}