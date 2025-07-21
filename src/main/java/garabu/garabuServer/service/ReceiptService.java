package garabu.garabuServer.service;

import garabu.garabuServer.domain.*;
import garabu.garabuServer.repository.*;
import garabu.garabuServer.dto.receipt.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReceiptService {
    
    private final ReceiptRepository receiptRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    
    // 파일 저장 경로 (application.yml에서 설정)
    private final String uploadPath = "/upload/receipts";
    
    public List<ReceiptResponse> getReceiptsByBook(String email, Long bookId) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("가계부를 찾을 수 없습니다."));
        
        // 권한 체크
        if (!book.hasMember(member)) {
            throw new IllegalArgumentException("해당 가계부에 접근 권한이 없습니다.");
        }
        
        List<Receipt> receipts = receiptRepository.findByBookOrderByCreatedAtDesc(book);
        
        return receipts.stream()
                .map(ReceiptResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public ReceiptResponse uploadReceipt(String email, Long bookId, MultipartFile file) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("가계부를 찾을 수 없습니다."));
        
        // 권한 체크
        if (!book.hasMember(member)) {
            throw new IllegalArgumentException("해당 가계부에 접근 권한이 없습니다.");
        }
        
        // 파일 검증
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }
        
        try {
            // 파일 저장
            String fileName = saveFile(file);
            String filePath = uploadPath + "/" + fileName;
            
            // Receipt 엔티티 생성
            Receipt receipt = new Receipt(
                book,
                member,
                fileName,
                filePath,
                contentType,
                file.getSize()
            );
            
            Receipt saved = receiptRepository.save(receipt);
            
            return ReceiptResponse.fromEntity(saved);
            
        } catch (IOException e) {
            log.error("파일 저장 실패", e);
            throw new RuntimeException("파일 저장에 실패했습니다.");
        }
    }
    
    @Transactional
    public void deleteReceipt(String email, Long receiptId) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new IllegalArgumentException("영수증을 찾을 수 없습니다."));
        
        // 권한 체크 (업로더 본인 또는 가계부 멤버)
        if (!receipt.getUploadedBy().equals(member) && 
            !receipt.getBook().hasMember(member)) {
            throw new IllegalArgumentException("해당 영수증에 대한 권한이 없습니다.");
        }
        
        // 파일 삭제
        try {
            Path filePath = Paths.get(receipt.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("파일 삭제 실패", e);
        }
        
        receiptRepository.delete(receipt);
    }
    
    private String saveFile(MultipartFile file) throws IOException {
        // 고유한 파일명 생성
        String extension = getFileExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString() + extension;
        
        // 디렉토리 생성
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        // 파일 저장
        Path filePath = uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
        
        return fileName;
    }
    
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : fileName.substring(lastDotIndex);
    }
} 