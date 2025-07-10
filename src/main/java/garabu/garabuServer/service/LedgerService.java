package garabu.garabuServer.service;

import garabu.garabuServer.domain.Ledger;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.dto.LedgerSearchConditionDTO;
import garabu.garabuServer.mapper.LedgerMapper;
import garabu.garabuServer.repository.LedgerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

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

    public Page<Ledger> search(LedgerSearchConditionDTO cond,
                               Pageable pageable) {

        long total = ledgerMapper.countLedgers(cond);

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

        List<Ledger> content = ledgerMapper.selectLedgers(cond, orderBy, offset, limit);

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 새로운 가계부 기록을 등록합니다.
     * 
     * @param ledger 등록할 가계부 기록 정보
     * @return 등록된 가계부 기록의 ID
     */
    public Long registLedger (Ledger ledger){
        ledgerJpaRepository.save(ledger);
        return ledger.getId();
    }

    /**
     * 특정 회원의 모든 가계부 기록을 조회합니다.
     * 
     * @param member 조회할 회원 정보
     * @return 해당 회원의 가계부 기록 목록
     */
    public List<Ledger> findAllLedgersByMember(Member member) {
        return ledgerJpaRepository.findByMember(member);
    }

    /**
     * 모든 가계부 기록을 조회합니다.
     * 
     * @return 전체 가계부 기록 목록
     */
    public List<Ledger> findAllLedgers() {
         return ledgerJpaRepository.findAll();
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
    public boolean existsRecentDuplicate(LocalDate date, Integer amount, String description, 
                                       Long memberId, Long bookId) {
        // 중복 기록 확인 (시간 기준 없이 단순 중복 확인)
        return ledgerJpaRepository.existsByDateAndAmountAndDescriptionAndMemberIdAndBookId(
            date, amount, description, memberId, bookId
        );
    }
}
