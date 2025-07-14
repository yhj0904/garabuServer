package garabu.garabuServer.api;

import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.BookRole;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.domain.PaymentMethod;
import garabu.garabuServer.domain.UserBook;
import garabu.garabuServer.dto.PaymentMethodDto;
import garabu.garabuServer.exception.BookNotFoundException;
import garabu.garabuServer.exception.DuplicateResourceException;
import garabu.garabuServer.service.BookService;
import garabu.garabuServer.service.PaymentService;
import garabu.garabuServer.service.UserBookService;
import garabu.garabuServer.jwt.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PaymentApiController TDD 테스트
 * 
 * 테스트 시나리오:
 * 1. 인증된 사용자가 자신의 가계부에 있는 결제 수단 목록을 조회할 수 있다
 * 2. 접근 권한이 없는 가계부의 결제 수단 조회 시 403 에러가 발생한다
 * 3. 존재하지 않는 가계부 ID로 조회 시 404 에러가 발생한다
 * 4. 인증되지 않은 사용자의 접근 시 401 에러가 발생한다
 */
@WebMvcTest(PaymentApiController.class)
class PaymentApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private BookService bookService;

    @MockBean
    private UserBookService userBookService;

    @MockBean
    private JWTUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private Book testBook;
    private Member testMember;
    private PaymentMethod testPayment1;
    private PaymentMethod testPayment2;
    private List<PaymentMethodDto> testPaymentDtos;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 설정
        testMember = new Member();
        testMember.setId(1L);
        testMember.setUsername("testuser");

        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("테스트 가계부");

        testPayment1 = new PaymentMethod();
        testPayment1.setId(1L);
        testPayment1.setPayment("현금");
        testPayment1.setBook(testBook);

        testPayment2 = new PaymentMethod();
        testPayment2.setId(2L);
        testPayment2.setPayment("카드");
        testPayment2.setBook(testBook);

        testPaymentDtos = Arrays.asList(
            new PaymentMethodDto(1L, "현금", 1L),
            new PaymentMethodDto(2L, "카드", 1L)
        );
    }

    @Test
    @DisplayName("인증된 사용자가 가계부의 결제 수단 목록을 성공적으로 조회한다")
    @WithMockUser(username = "testuser")
    void listPaymentsByBook_Success() throws Exception {
        // given
        Long bookId = 1L;
        when(bookService.findById(bookId)).thenReturn(testBook);
        when(paymentService.findByBookDto(testBook)).thenReturn(testPaymentDtos);
        doNothing().when(userBookService).validateBookAccess(testBook);

        // when & then
        mockMvc.perform(get("/api/v2/payment/book/{bookId}", bookId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payments").isArray())
                .andExpect(jsonPath("$.payments[0].id").value(1))
                .andExpect(jsonPath("$.payments[0].payment").value("현금"))
                .andExpect(jsonPath("$.payments[1].id").value(2))
                .andExpect(jsonPath("$.payments[1].payment").value("카드"));

        // verify
        verify(bookService).findById(bookId);
        verify(paymentService).findByBookDto(testBook);
        verify(userBookService).validateBookAccess(testBook);
    }

    @Test
    @DisplayName("접근 권한이 없는 가계부의 결제 수단 조회 시 403 에러가 발생한다")
    @WithMockUser(username = "testuser")
    void listPaymentsByBook_AccessDenied() throws Exception {
        // Given: 사용자가 가계부에 접근 권한이 없다
        when(bookService.findById(1L)).thenReturn(testBook);
        doThrow(new IllegalArgumentException("해당 가계부에 대한 접근 권한이 없습니다."))
                .when(userBookService).validateBookAccess(testBook);

        // When & Then: 400 에러가 발생한다 (IllegalArgumentException은 400으로 처리됨)
        mockMvc.perform(get("/api/v2/payment/book/1")
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(bookService).findById(1L);
        verify(userBookService).validateBookAccess(testBook);
        verify(paymentService, never()).findByBookDto(any());
    }

    @Test
    @DisplayName("존재하지 않는 가계부 ID로 조회 시 404 에러가 발생한다")
    @WithMockUser(username = "testuser")
    void listPaymentsByBook_BookNotFound() throws Exception {
        // Given: 존재하지 않는 가계부 ID
        when(bookService.findById(999L)).thenThrow(new BookNotFoundException(999L));

        // When & Then: 404 에러가 발생한다
        mockMvc.perform(get("/api/v2/payment/book/999")
                .with(csrf()))
                .andExpect(status().isNotFound());

        verify(bookService).findById(999L);
        verify(userBookService, never()).validateBookAccess(any());
        verify(paymentService, never()).findByBookDto(any());
    }

    @Test
    @DisplayName("인증되지 않은 사용자의 접근 시 401 에러가 발생한다")
    void listPaymentsByBook_Unauthorized() throws Exception {
        // When & Then: 인증 없이 API 호출 시 401 에러가 발생한다
        mockMvc.perform(get("/api/v2/payment/book/1"))
                .andExpect(status().isFound()); // Spring Security 기본 설정으로 로그인 페이지로 리다이렉트

        // 서비스 메서드들이 호출되지 않았는지 검증
        verify(bookService, never()).findById(any());
        verify(userBookService, never()).validateBookAccess(any());
        verify(paymentService, never()).findByBookDto(any());
    }

    @Test
    @DisplayName("가계부에 새로운 결제 수단을 성공적으로 생성한다")
    @WithMockUser(username = "testuser")
    void createPaymentForBook_Success() throws Exception {
        // Given: 사용자가 가계부에 편집 권한이 있고 결제 수단이 중복되지 않는다
        when(bookService.findById(1L)).thenReturn(testBook);
        doNothing().when(userBookService).validateBookEditAccess(testBook);
        when(paymentService.findByBookAndPayment(testBook, "신용카드")).thenReturn(null);
        when(paymentService.createPaymentForBook(testBook, "신용카드")).thenReturn(3L);

        CreatePaymentRequest request = new CreatePaymentRequest("신용카드");
        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then: API 호출이 성공하고 새로운 결제 수단이 생성된다
        mockMvc.perform(post("/api/v2/payment/book/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(3));

        verify(bookService).findById(1L);
        verify(userBookService).validateBookEditAccess(testBook);
        verify(paymentService).findByBookAndPayment(testBook, "신용카드");
        verify(paymentService).createPaymentForBook(testBook, "신용카드");
    }

    @Test
    @DisplayName("중복된 결제 수단 생성 시 400 에러가 발생한다")
    @WithMockUser(username = "testuser")
    void createPaymentForBook_DuplicatePayment() throws Exception {
        // Given: 이미 존재하는 결제 수단과 동일한 이름으로 생성 시도
        when(bookService.findById(1L)).thenReturn(testBook);
        doNothing().when(userBookService).validateBookEditAccess(testBook);
        when(paymentService.findByBookAndPayment(testBook, "현금")).thenReturn(testPayment1);

        CreatePaymentRequest request = new CreatePaymentRequest("현금");
        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then: 409 Conflict 에러가 발생한다 (DuplicateResourceException)
        mockMvc.perform(post("/api/v2/payment/book/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isConflict());

        verify(bookService).findById(1L);
        verify(userBookService).validateBookEditAccess(testBook);
        verify(paymentService).findByBookAndPayment(testBook, "현금");
        verify(paymentService, never()).createPaymentForBook(any(), any());
    }

    /**
     * CreatePaymentRequest DTO for testing
     * PaymentApiController의 inner class를 테스트에서 사용하기 위한 임시 클래스
     */
    static class CreatePaymentRequest {
        private String payment;

        public CreatePaymentRequest(String payment) {
            this.payment = payment;
        }

        public String getPayment() {
            return payment;
        }

        public void setPayment(String payment) {
            this.payment = payment;
        }
    }
}