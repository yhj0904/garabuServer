package garabu.garabuServer.repository;

import garabu.garabuServer.domain.Asset;
import garabu.garabuServer.domain.AssetType;
import garabu.garabuServer.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 자산 JPA Repository
 */
public interface AssetJpaRepository extends JpaRepository<Asset, Long> {

    /**
     * 가계부별 자산 목록 조회 (활성화된 자산만)
     */
    List<Asset> findByBookAndIsActiveTrueOrderByCreatedAtDesc(Book book);

    /**
     * 가계부별 모든 자산 목록 조회
     */
    List<Asset> findByBookOrderByCreatedAtDesc(Book book);

    /**
     * 가계부별 자산 타입별 자산 목록 조회
     */
    List<Asset> findByBookAndAssetTypeAndIsActiveTrueOrderByCreatedAtDesc(Book book, AssetType assetType);

    /**
     * 가계부별 자산 이름으로 검색
     */
    Optional<Asset> findByBookAndNameAndIsActiveTrue(Book book, String name);

    /**
     * 가계부별 총 자산 계산
     */
    @Query("SELECT SUM(a.balance) FROM Asset a WHERE a.book = :book AND a.isActive = true")
    Long getTotalAssetsByBook(@Param("book") Book book);

    /**
     * 가계부별 자산 타입별 총액 계산
     */
    @Query("SELECT SUM(a.balance) FROM Asset a WHERE a.book = :book AND a.assetType = :assetType AND a.isActive = true")
    Long getTotalAssetsByBookAndType(@Param("book") Book book, @Param("assetType") AssetType assetType);

    /**
     * 가계부별 자산 개수 조회
     */
    Long countByBookAndIsActiveTrue(Book book);

    /**
     * 특정 자산이 해당 가계부에 속하는지 확인
     */
    boolean existsByIdAndBook(Long assetId, Book book);
}