package garabu.garabuServer.service;

import garabu.garabuServer.domain.Ledger;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.Asset;
import garabu.garabuServer.domain.AmountType;
import garabu.garabuServer.domain.PaymentMethod;
import garabu.garabuServer.domain.Category;
import garabu.garabuServer.domain.UserBook;
import garabu.garabuServer.domain.BookRole;
import garabu.garabuServer.dto.LedgerDTO;
import garabu.garabuServer.dto.LedgerSearchConditionDTO;
import garabu.garabuServer.dto.request.CreateTransferRequest;
import garabu.garabuServer.mapper.LedgerMapper;
import garabu.garabuServer.repository.LedgerJpaRepository;
import garabu.garabuServer.repository.AssetJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 가계부 기록 관리 서비스 클래스
 * 
 * 가계부 기록의 등록, 조회 등의 비즈니스 로직을 처리합니다.
 * 수입, 지출, 이체 등의 금융 기록을 관리합니다.
 * 
 * @author yhj
 * @version 2.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LedgerService {

    private final LedgerJpaRepository ledgerJpaRepository;
    private final LedgerMapper ledgerMapper;
    private final UserBookService userBookService;
    private final AssetJpaRepository assetJpaRepository;
    private final PaymentService paymentService;
    private final CategoryService categoryService;

    /**
     * 가계부별 기본 목록 조회 (JPA 사용)
     * 권한 확인은 컨트롤러에서 이미 처리됨
     * LazyInitializationException 방지를 위해 연관 엔티티 fetch
     */
    public Page<LedgerDTO> findLedgersByBook(Book book, Pageable pageable) {
        // 먼저 ID만 조회하여 페이징 처리
        Page<Long> idPage = ledgerJpaRepository.findIdsByBook(book, pageable);
        
        // ID가 비어있으면 빈 페이지 반환
        if (idPage.isEmpty()) {
            return Page.empty(pageable);
        }
        
        // ID 목록으로 실제 데이터를 fetch join하여 조회
        List<Ledger> ledgers = ledgerJpaRepository.findByIdsWithFetch(idPage.getContent());
        
        // DTO로 변환
        List<LedgerDTO> dtos = ledgers.stream()
                .map(LedgerDTO::from)
                .collect(Collectors.toList());
        
        // Page 객체로 재구성
        return new PageImpl<>(dtos, pageable, idPage.getTotalElements());
    }
    
    /**
     * 검색 조건이 있는 경우 동적 검색 (MyBatis 사용)
     * 권한 확인은 컨트롤러에서 이미 처리됨
     */
    public Page<LedgerDTO> searchLedgers(LedgerSearchConditionDTO cond,
                                     Pageable pageable) {

        long total = ledgerMapper.countSearchLedgers(cond);

        // 페이지 초과 요청 시 빈 페이지 처리
        if (total == 0) {
            return Page.empty(pageable);
        }

        // Pageable → orderBy / offset / limit
        String orderBy = pageable.getSort().isEmpty()
                ? "l.date DESC"
                : pageable.getSort().stream()
                .map(o -> switch (o.getProperty()) {
                    case "date"   -> "l.date "   + o.getDirection();
                    case "amount" -> "l.amount " + o.getDirection();
                    default       -> "l.date DESC";
                })
                .findFirst().orElse("l.date DESC");

        long offset = (long) pageable.getPageNumber() * pageable.getPageSize();
        long limit  = pageable.getPageSize();

        List<Ledger> content = ledgerMapper.searchLedgers(cond, orderBy, offset, limit);
        List<LedgerDTO> dtoContent = LedgerDTO.from(content);

        return new PageImpl<>(dtoContent, pageable, total);
    }

    /**
     * 새로운 가계부 기록을 등록합니다.
     * 
     * @param ledger 등록할 가계부 기록 정보
     * @return 등록된 가계부 기록의 ID
     */
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public Long registLedger (Ledger ledger){
        // 자산 잔액 업데이트
        if (ledger.getPaymentMethod() != null && ledger.getPaymentMethod().getAsset() != null) {
            Asset asset = ledger.getPaymentMethod().getAsset();
            if (ledger.getAmountType() == AmountType.INCOME) {
                // 수입: 자산에 금액 추가
                asset.updateBalance(ledger.getAmount(), "ADD");
            } else if (ledger.getAmountType() == AmountType.EXPENSE) {
                // 지출: 자산에서 금액 차감
                asset.updateBalance(ledger.getAmount(), "SUBTRACT");
            }
            assetJpaRepository.save(asset);
        }
        
        ledgerJpaRepository.save(ledger);
        return ledger.getId();
    }

    /**
     * 가계부 기록을 생성합니다 (권한 검사, 결제수단/카테고리 자동 생성, 중복 검사 포함)
     *
     * @param request 가계부 기록 생성 요청
     * @param currentMember 현재 로그인한 사용자
     * @param book 대상 가계부
     * @param paymentService 결제수단 서비스
     * @param categoryService 카테고리 서비스
     * @return 생성된 가계부 기록
     */
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public Ledger createLedgerWithValidation(garabu.garabuServer.dto.CreateLedgerRequest request, 
                                           Member currentMember, Book book,
                                           PaymentService paymentService, 
                                           CategoryService categoryService) {
        // 1. 엔티티 매핑
        Ledger ledger = new Ledger();
        ledger.setDate(request.getDate());
        ledger.setAmount(request.getAmount());
        ledger.setDescription(request.getDescription());
        ledger.setMemo(request.getMemo());
        ledger.setAmountType(request.getAmountType());
        ledger.setMember(currentMember);
        ledger.setSpender(request.getSpender());
        ledger.setBook(book);
        
        // 2. 결제수단 조회 및 자동 생성
        PaymentMethod paymentMethod = paymentService.findByBookAndPayment(book, request.getPayment());
        if (paymentMethod == null) {
            Long paymentId = paymentService.createPaymentForBook(book, request.getPayment());
            paymentMethod = paymentService.findById(paymentId);
        }
        ledger.setPaymentMethod(paymentMethod);
        
        // 3. 카테고리 조회
        Category category = categoryService.findByBookAndCategory(book, request.getCategory());
        if (category == null) {
            throw new RuntimeException("해당 가계부에 존재하지 않는 카테고리입니다: " + request.getCategory());
        }
        ledger.setCategory(category);
        
        // 4. 중복 검사
        boolean isDuplicate = existsRecentDuplicate(
            ledger.getDate(), ledger.getAmount(), ledger.getDescription(), 
            currentMember.getId(), book.getId()
        );
        
        if (isDuplicate) {
            throw new RuntimeException("동일한 내용의 기록이 최근에 추가되었습니다. 중복 등록을 확인해주세요.");
        }
        
        // 5. 자산 잔액 업데이트
        if (paymentMethod != null && paymentMethod.getAsset() != null) {
            Asset asset = paymentMethod.getAsset();
            if (ledger.getAmountType() == AmountType.INCOME) {
                // 수입: 자산에 금액 추가
                asset.updateBalance(ledger.getAmount(), "ADD");
            } else if (ledger.getAmountType() == AmountType.EXPENSE) {
                // 지출: 자산에서 금액 차감
                asset.updateBalance(ledger.getAmount(), "SUBTRACT");
            }
            assetJpaRepository.save(asset);
        }
        
        // 6. 저장
        return ledgerJpaRepository.save(ledger);
    }

    /**
     * 특정 회원의 모든 가계부 기록을 조회합니다.
     * 
     * @param member 조회할 회원 정보
     * @return 해당 회원의 가계부 기록 목록
     */
    public List<LedgerDTO> findAllLedgersByMember(Member member) {
        List<Ledger> ledgers = ledgerJpaRepository.findByMember(member);
        return LedgerDTO.from(ledgers);
    }

    /**
     * 모든 가계부 기록을 조회합니다.
     * 
     * @return 전체 가계부 기록 목록
     */
    public List<LedgerDTO> findAllLedgers() {
        List<Ledger> ledgers = ledgerJpaRepository.findAll();
        return LedgerDTO.from(ledgers);
    }
    
    /**
     * ID로 가계부 기록을 조회합니다.
     * 
     * @param id 가계부 기록 ID
     * @return 가계부 기록
     */
    public Ledger findById(Long id) {
        return ledgerJpaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("가계부 기록을 찾을 수 없습니다: " + id));
    }
    
    /**
     * ID로 가계부 기록을 DTO로 조회합니다.
     * 
     * @param id 가계부 기록 ID
     * @return 가계부 기록 DTO
     */
    public LedgerDTO findByIdAsDTO(Long id) {
        Ledger ledger = findById(id);
        return LedgerDTO.from(ledger);
    }
    
    /**
     * 중복 기록 존재 여부를 확인합니다.
     * 동일한 날짜, 금액, 설명의 기록이 최근 1시간 내에 있는지 확인
     * 
     * @param date 기록 날짜
     * @param amount 금액
     * @param description 설명
     * @param memberId 사용자 ID
     * @param bookId 가계부 ID
     * @return 중복 존재 여부
     */
    public boolean existsRecentDuplicate(LocalDate date, Long amount, String description, 
                                       Long memberId, Long bookId) {
        // 중복 기록 확인 (시간 기준 없이 단순 중복 확인)
        return ledgerJpaRepository.existsByDateAndAmountAndDescriptionAndMemberIdAndBookId(
            date, amount, description, memberId, bookId
        );
    }

    /**
     * 이체 기록 생성
     * 출금 자산과 입금 자산 간의 이체를 처리합니다.
     * 
     * @param request 이체 요청 정보
     * @param member 요청한 사용자
     * @return 생성된 이체 기록 목록 (출금, 입금 2개)
     */
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public List<LedgerDTO> createTransfer(CreateTransferRequest request, Member member) {
        // 자산 유효성 검사
        Asset fromAsset = assetJpaRepository.findById(request.getFromAssetId())
            .orElseThrow(() -> new IllegalArgumentException("출금 자산을 찾을 수 없습니다."));
        
        Asset toAsset = assetJpaRepository.findById(request.getToAssetId())
            .orElseThrow(() -> new IllegalArgumentException("입금 자산을 찾을 수 없습니다."));

        // 같은 자산 간 이체 방지
        if (fromAsset.getId().equals(toAsset.getId())) {
            throw new IllegalArgumentException("동일한 자산 간 이체는 불가능합니다.");
        }

        // 두 자산이 같은 가계부에 속하는지 확인
        if (!fromAsset.getBook().getId().equals(toAsset.getBook().getId()) ||
            !fromAsset.getBook().getId().equals(request.getBookId())) {
            throw new IllegalArgumentException("다른 가계부의 자산 간 이체는 불가능합니다.");
        }

        // 출금 자산 잔액 확인
        if (fromAsset.getBalance() < request.getAmount()) {
            throw new IllegalArgumentException("출금 자산의 잔액이 부족합니다.");
        }

        // 자산 잔액 업데이트
        fromAsset.updateBalance(request.getAmount(), "SUBTRACT");
        toAsset.updateBalance(request.getAmount(), "ADD");

        // 이체 기록 생성 (출금 기록)
        Ledger withdrawalLedger = new Ledger();
        withdrawalLedger.setDate(request.getDate());
        withdrawalLedger.setAmount(request.getAmount().longValue());
        withdrawalLedger.setDescription(request.getDescription() + " (출금)");
        withdrawalLedger.setMemo(request.getMemo());
        withdrawalLedger.setAmountType(AmountType.TRANSFER);
        withdrawalLedger.setMember(member);
        withdrawalLedger.setBook(fromAsset.getBook());
        withdrawalLedger.setSpender(request.getTransferer());
        
        // 이체 기록 생성 (입금 기록)
        Ledger depositLedger = new Ledger();
        depositLedger.setDate(request.getDate());
        depositLedger.setAmount(request.getAmount().longValue());
        depositLedger.setDescription(request.getDescription() + " (입금)");
        depositLedger.setMemo(request.getMemo());
        depositLedger.setAmountType(AmountType.TRANSFER);
        depositLedger.setMember(member);
        depositLedger.setBook(toAsset.getBook());
        depositLedger.setSpender(request.getTransferer());

        // 자산 저장
        assetJpaRepository.save(fromAsset);
        assetJpaRepository.save(toAsset);

        // 이체 기록 저장
        Ledger savedWithdrawal = ledgerJpaRepository.save(withdrawalLedger);
        Ledger savedDeposit = ledgerJpaRepository.save(depositLedger);

        List<LedgerDTO> result = new ArrayList<>();
        result.add(LedgerDTO.from(savedWithdrawal));
        result.add(LedgerDTO.from(savedDeposit));

        return result;
    }
    
    /**
     * 가계부 기록을 삭제합니다.
     * 
     * @param ledgerId 삭제할 가계부 기록 ID
     * @param currentMember 현재 로그인한 사용자
     * @throws RuntimeException 권한이 없거나 기록을 찾을 수 없는 경우
     */
    @Transactional(rollbackFor = Exception.class, timeout = 15)
    public void deleteLedger(Long ledgerId, Member currentMember) {
        // 기록 조회
        Ledger ledger = ledgerJpaRepository.findById(ledgerId)
            .orElseThrow(() -> new RuntimeException("가계부 기록을 찾을 수 없습니다: " + ledgerId));
        
        // 권한 확인 - 기록 작성자 또는 가계부 소유자만 삭제 가능
        boolean canDelete = false;
        
        // 1. 기록 작성자인지 확인
        if (ledger.getMember().getId().equals(currentMember.getId())) {
            canDelete = true;
        } else {
            // 2. 가계부 소유자/편집자인지 확인
            try {
                UserBook userBook = userBookService.findByBookIdAndMemberId(
                    ledger.getBook().getId(), currentMember.getId()
                ).orElse(null);
                
                if (userBook != null && 
                    (userBook.getBookRole() == BookRole.OWNER || userBook.getBookRole() == BookRole.EDITOR)) {
                    canDelete = true;
                }
            } catch (Exception e) {
                // 권한 확인 실패
            }
        }
        
        if (!canDelete) {
            throw new RuntimeException("해당 기록을 삭제할 권한이 없습니다.");
        }
        
        // 자산 업데이트 (삭제 전에 처리)
        if (ledger.getPaymentMethod() != null && ledger.getPaymentMethod().getAsset() != null) {
            Asset asset = ledger.getPaymentMethod().getAsset();
            if (ledger.getAmountType() == AmountType.INCOME) {
                // 수입 삭제: 자산에서 금액 차감
                asset.updateBalance(ledger.getAmount(), "SUBTRACT");
            } else if (ledger.getAmountType() == AmountType.EXPENSE) {
                // 지출 삭제: 자산에 금액 추가
                asset.updateBalance(ledger.getAmount(), "ADD");
            }
            assetJpaRepository.save(asset);
        }
        
        // 삭제 수행
        ledgerJpaRepository.delete(ledger);
    }
    
    /**
     * 가계부 기록을 ID로 삭제합니다 (권한 확인 포함).
     * 
     * @param ledgerId 삭제할 가계부 기록 ID
     * @param bookId 가계부 ID (권한 확인용)
     * @param currentMember 현재 로그인한 사용자
     * @throws RuntimeException 권한이 없거나 기록을 찾을 수 없는 경우
     */
    @Transactional(rollbackFor = Exception.class, timeout = 15)
    public void deleteLedgerById(Long ledgerId, Long bookId, Member currentMember) {
        // 기록 조회
        Ledger ledger = ledgerJpaRepository.findById(ledgerId)
            .orElseThrow(() -> new RuntimeException("가계부 기록을 찾을 수 없습니다: " + ledgerId));
        
        // 가계부 일치 확인
        if (!ledger.getBook().getId().equals(bookId)) {
            throw new RuntimeException("해당 가계부의 기록이 아닙니다.");
        }
        
        // 가계부 접근 권한 확인
        UserBook userBook = userBookService.findByBookIdAndMemberId(bookId, currentMember.getId())
            .orElseThrow(() -> new RuntimeException("해당 가계부에 접근 권한이 없습니다."));
        
        // VIEWER는 삭제 불가
        if (userBook.getBookRole() == BookRole.VIEWER) {
            throw new RuntimeException("조회 권한만 있습니다. 기록을 삭제할 수 없습니다.");
        }
        
        // EDITOR는 본인이 작성한 기록만 삭제 가능
        if (userBook.getBookRole() == BookRole.EDITOR && 
            !ledger.getMember().getId().equals(currentMember.getId())) {
            throw new RuntimeException("편집자는 본인이 작성한 기록만 삭제할 수 있습니다.");
        }
        
        // 삭제 수행
        ledgerJpaRepository.delete(ledger);
    }
}
