package garabu.garabuServer.service;

import garabu.garabuServer.domain.Asset;
import garabu.garabuServer.domain.AssetType;
import garabu.garabuServer.domain.Book;
import garabu.garabuServer.dto.request.CreateAssetRequest;
import garabu.garabuServer.dto.request.UpdateAssetBalanceRequest;
import garabu.garabuServer.dto.request.UpdateAssetRequest;
import garabu.garabuServer.dto.response.AssetResponse;
import garabu.garabuServer.repository.AssetJpaRepository;
import garabu.garabuServer.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 자산 서비스
 */
@Service
@Transactional(readOnly = true)
public class AssetService {

    private final AssetJpaRepository assetRepository;
    private final BookRepository bookRepository;

    @Autowired
    public AssetService(AssetJpaRepository assetRepository, BookRepository bookRepository) {
        this.assetRepository = assetRepository;
        this.bookRepository = bookRepository;
    }

    /**
     * 자산 생성
     */
    @Transactional
    public AssetResponse createAsset(Long bookId, CreateAssetRequest request) {
        // 가계부 조회
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 가계부입니다."));

        // 동일한 이름의 자산이 이미 존재하는지 확인
        if (assetRepository.findByBookAndNameAndIsActiveTrue(book, request.getName()).isPresent()) {
            throw new IllegalArgumentException("동일한 이름의 자산이 이미 존재합니다.");
        }

        // 자산 생성
        Asset asset = new Asset(request.getName(), request.getAssetType(), request.getBalance(), book);
        asset.setDescription(request.getDescription());
        asset.setAccountNumber(request.getAccountNumber());
        asset.setBankName(request.getBankName());
        asset.setCardType(request.getCardType());

        Asset savedAsset = assetRepository.save(asset);
        return new AssetResponse(savedAsset);
    }

    /**
     * 가계부별 자산 목록 조회 (활성화된 자산만)
     */
    public List<AssetResponse> getAssetsByBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 가계부입니다."));

        List<Asset> assets = assetRepository.findByBookAndIsActiveTrueOrderByCreatedAtDesc(book);
        return assets.stream()
                .map(AssetResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 가계부별 자산 타입별 자산 목록 조회
     */
    public List<AssetResponse> getAssetsByBookAndType(Long bookId, AssetType assetType) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 가계부입니다."));

        List<Asset> assets = assetRepository.findByBookAndAssetTypeAndIsActiveTrueOrderByCreatedAtDesc(book, assetType);
        return assets.stream()
                .map(AssetResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 자산 상세 조회
     */
    public AssetResponse getAsset(Long assetId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 자산입니다."));

        return new AssetResponse(asset);
    }

    /**
     * 자산 정보 수정
     */
    @Transactional
    public AssetResponse updateAsset(Long assetId, UpdateAssetRequest request) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 자산입니다."));

        // 이름 중복 체크 (변경하려는 이름이 다른 자산에서 사용 중인지)
        if (request.getName() != null && !request.getName().equals(asset.getName())) {
            if (assetRepository.findByBookAndNameAndIsActiveTrue(asset.getBook(), request.getName()).isPresent()) {
                throw new IllegalArgumentException("동일한 이름의 자산이 이미 존재합니다.");
            }
        }

        // 정보 업데이트
        asset.updateInfo(request.getName(), request.getDescription(), request.getAccountNumber(),
                request.getBankName(), request.getCardType());

        // 활성화 상태 업데이트
        if (request.getIsActive() != null) {
            if (request.getIsActive()) {
                asset.activate();
            } else {
                asset.deactivate();
            }
        }

        Asset updatedAsset = assetRepository.save(asset);
        return new AssetResponse(updatedAsset);
    }

    /**
     * 자산 잔액 수정
     */
    @Transactional
    public AssetResponse updateAssetBalance(Long assetId, UpdateAssetBalanceRequest request) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 자산입니다."));

        // 잔액 업데이트
        asset.updateBalance(request.getAmount(), request.getOperation());

        Asset updatedAsset = assetRepository.save(asset);
        return new AssetResponse(updatedAsset);
    }

    /**
     * 자산 삭제 (실제로는 비활성화)
     */
    @Transactional
    public void deleteAsset(Long assetId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 자산입니다."));

        asset.deactivate();
        assetRepository.save(asset);
    }

    /**
     * 가계부별 총 자산 계산
     */
    public Long getTotalAssetsByBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 가계부입니다."));

        Long totalAssets = assetRepository.getTotalAssetsByBook(book);
        return totalAssets != null ? totalAssets : 0L;
    }

    /**
     * 특정 자산이 해당 가계부에 속하는지 확인
     */
    public boolean validateAssetAccess(Long assetId, Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 가계부입니다."));

        return assetRepository.existsByIdAndBook(assetId, book);
    }

    /**
     * 자산 이름으로 자산 조회 (거래 생성 시 사용)
     */
    public Asset findAssetByNameAndBook(String assetName, Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 가계부입니다."));

        return assetRepository.findByBookAndNameAndIsActiveTrue(book, assetName)
                .orElse(null); // 없으면 null 반환 (기존 PaymentMethod 방식과 호환)
    }
}