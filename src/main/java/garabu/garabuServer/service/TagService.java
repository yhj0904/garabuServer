package garabu.garabuServer.service;

import garabu.garabuServer.domain.*;
import garabu.garabuServer.dto.tag.*;
import garabu.garabuServer.exception.BookAccessException;
import garabu.garabuServer.exception.BookNotFoundException;
import garabu.garabuServer.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {
    
    private final TagRepository tagRepository;
    private final BookRepository bookRepository;
    private final UserBookJpaRepository userBookRepository;
    private final LedgerJpaRepository ledgerRepository;
    private final MemberService memberService;
    
    @Transactional
    public TagResponse createTag(Long bookId, CreateTagRequest request) {
        Member currentMember = memberService.getCurrentMember();
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("가계부를 찾을 수 없습니다."));
        
        checkBookAccess(book, currentMember);
        
        // 중복 태그 확인
        if (tagRepository.findByBookIdAndName(bookId, request.getName()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 태그입니다.");
        }
        
        Tag tag = new Tag(book, request.getName(), request.getColor());
        Tag savedTag = tagRepository.save(tag);
        
        return convertToResponse(savedTag);
    }
    
    public List<TagResponse> getTags(Long bookId) {
        Member currentMember = memberService.getCurrentMember();
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("가계부를 찾을 수 없습니다."));
        
        checkBookAccess(book, currentMember);
        
        List<Tag> tags = tagRepository.findByBookIdOrderByUsageCountDesc(bookId);
        
        return tags.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public TagResponse updateTag(Long tagId, UpdateTagRequest request) {
        Member currentMember = memberService.getCurrentMember();
        
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("태그를 찾을 수 없습니다."));
        
        checkBookAccess(tag.getBook(), currentMember);
        
        if (request.getName() != null) {
            // 중복 태그 확인
            tagRepository.findByBookIdAndName(tag.getBook().getId(), request.getName())
                    .ifPresent(existingTag -> {
                        if (!existingTag.getId().equals(tag.getId())) {
                            throw new IllegalArgumentException("이미 존재하는 태그 이름입니다.");
                        }
                    });
            tag.setName(request.getName());
        }
        
        if (request.getColor() != null) {
            tag.setColor(request.getColor());
        }
        
        Tag updatedTag = tagRepository.save(tag);
        return convertToResponse(updatedTag);
    }
    
    @Transactional
    public void deleteTag(Long tagId) {
        Member currentMember = memberService.getCurrentMember();
        
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("태그를 찾을 수 없습니다."));
        
        checkBookAccess(tag.getBook(), currentMember);
        
        // 연결된 거래 내역에서 태그 제거
        for (Ledger ledger : new ArrayList<>(tag.getLedgers())) {
            ledger.removeTag(tag);
        }
        
        tagRepository.delete(tag);
    }
    
    @Transactional
    public void addTagToTransaction(Long ledgerId, Long tagId) {
        Member currentMember = memberService.getCurrentMember();
        
        Ledger ledger = ledgerRepository.findById(ledgerId)
                .orElseThrow(() -> new IllegalArgumentException("거래 내역을 찾을 수 없습니다."));
        
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("태그를 찾을 수 없습니다."));
        
        checkBookAccess(ledger.getBook(), currentMember);
        checkBookAccess(tag.getBook(), currentMember);
        
        if (!ledger.getBook().getId().equals(tag.getBook().getId())) {
            throw new IllegalArgumentException("같은 가계부의 태그만 추가할 수 있습니다.");
        }
        
        tag.addLedger(ledger);
        tagRepository.save(tag);
    }
    
    @Transactional
    public void removeTagFromTransaction(Long ledgerId, Long tagId) {
        Member currentMember = memberService.getCurrentMember();
        
        Ledger ledger = ledgerRepository.findById(ledgerId)
                .orElseThrow(() -> new IllegalArgumentException("거래 내역을 찾을 수 없습니다."));
        
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("태그를 찾을 수 없습니다."));
        
        checkBookAccess(ledger.getBook(), currentMember);
        
        tag.removeLedger(ledger);
        tagRepository.save(tag);
    }
    
    public TagSearchResponse searchTransactionsByTag(Long bookId, String tagName, Pageable pageable) {
        Member currentMember = memberService.getCurrentMember();
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("가계부를 찾을 수 없습니다."));
        
        checkBookAccess(book, currentMember);
        
        Tag tag = tagRepository.findByBookIdAndName(bookId, tagName)
                .orElseThrow(() -> new IllegalArgumentException("태그를 찾을 수 없습니다."));
        
        // 태그가 연결된 거래 내역을 페이지네이션하여 조회
        List<Ledger> ledgers = new ArrayList<>(tag.getLedgers());
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), ledgers.size());
        
        List<TransactionSummary> transactions = ledgers.subList(start, end).stream()
                .map(this::convertToTransactionSummary)
                .collect(Collectors.toList());
        
        TagSearchResponse response = new TagSearchResponse();
        response.setTagName(tag.getName());
        response.setTagColor(tag.getColor());
        response.setTransactions(transactions);
        response.setTotalElements((long) ledgers.size());
        response.setTotalPages((int) Math.ceil((double) ledgers.size() / pageable.getPageSize()));
        response.setCurrentPage(pageable.getPageNumber());
        
        return response;
    }
    
    @Transactional
    public void mergeTags(Long sourceTagId, Long targetTagId) {
        Member currentMember = memberService.getCurrentMember();
        
        Tag sourceTag = tagRepository.findById(sourceTagId)
                .orElseThrow(() -> new IllegalArgumentException("원본 태그를 찾을 수 없습니다."));
        
        Tag targetTag = tagRepository.findById(targetTagId)
                .orElseThrow(() -> new IllegalArgumentException("대상 태그를 찾을 수 없습니다."));
        
        checkBookAccess(sourceTag.getBook(), currentMember);
        checkBookAccess(targetTag.getBook(), currentMember);
        
        if (!sourceTag.getBook().getId().equals(targetTag.getBook().getId())) {
            throw new IllegalArgumentException("같은 가계부의 태그만 병합할 수 있습니다.");
        }
        
        // 원본 태그의 모든 거래 내역을 대상 태그로 이동
        for (Ledger ledger : new ArrayList<>(sourceTag.getLedgers())) {
            sourceTag.removeLedger(ledger);
            targetTag.addLedger(ledger);
        }
        
        tagRepository.save(targetTag);
        tagRepository.delete(sourceTag);
    }
    
    private void checkBookAccess(Book book, Member member) {
        boolean hasAccess = userBookRepository.findByBookIdAndMemberId(book.getId(), member.getId())
                .map(userBook -> !userBook.getBookRole().equals(BookRole.VIEWER))
                .orElse(false);
        
        if (!hasAccess) {
            throw new BookAccessException("이 가계부에 접근할 권한이 없습니다.");
        }
    }
    
    private TagResponse convertToResponse(Tag tag) {
        TagResponse response = new TagResponse();
        response.setId(tag.getId());
        response.setName(tag.getName());
        response.setColor(tag.getColor());
        response.setUsageCount(tag.getUsageCount());
        response.setCreatedAt(tag.getCreatedAt());
        
        return response;
    }
    
    private TransactionSummary convertToTransactionSummary(Ledger ledger) {
        TransactionSummary summary = new TransactionSummary();
        summary.setId(ledger.getId());
        summary.setDate(ledger.getDate());
        summary.setAmount(ledger.getAmount());
        summary.setDescription(ledger.getDescription());
        summary.setAmountType(ledger.getAmountType());
        
        if (ledger.getCategory() != null) {
            summary.setCategoryName(ledger.getCategory().getCategory());
        }
        
        return summary;
    }
} 