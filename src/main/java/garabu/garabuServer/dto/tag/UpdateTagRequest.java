package garabu.garabuServer.dto.tag;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateTagRequest {
    
    @Size(min = 1, max = 20, message = "태그 이름은 1-20자 사이여야 합니다")
    private String name;
    
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "색상은 #RRGGBB 형식이어야 합니다")
    private String color;
} 