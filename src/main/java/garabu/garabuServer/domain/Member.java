package garabu.garabuServer.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 회원 엔티티 클래스
 * 
 * 시스템에 가입한 사용자의 정보를 관리합니다.
 * 회원의 기본 정보와 가계부 연관 관계를 포함합니다.
 * 
 * @author Garabu Team
 * @version 1.0
 */
@Entity
@Getter @Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;            // 회원 식별자 Id

    private String username;        // 닉네임
    private String name;            // 실제 이름
    @Email
    private String email;       // 이메일
    private String password;         // 비번
    
    @Enumerated(EnumType.STRING)
    private SystemRole systemRole = SystemRole.ROLE_USER; // 기본값 USER로 설정
    
    private String providerId;      // OAuth2 제공자 ID

    /**
     * 회원이 소유한 가계부 목록
     * OneToMany 관계로 회원과 가계부는 1:N 관계
     */
    @OneToMany(mappedBy = "member")
    @JsonManagedReference("member-userBooks")
    private List<UserBook> userBooks = new ArrayList<>();
    
    // SystemRole을 String으로 변환하여 반환하는 메서드 (기존 코드와의 호환성)
    public String getRole() {
        return systemRole != null ? systemRole.name() : SystemRole.ROLE_USER.name();
    }
    
    // String을 SystemRole로 변환하여 설정하는 메서드 (기존 코드와의 호환성)
    public void setRole(String role) {
        this.systemRole = SystemRole.valueOf(role);
    }
}
