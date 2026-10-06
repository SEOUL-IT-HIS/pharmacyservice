package kr.co.seoulit.his.pharmacyservice.inventory.repository;

import kr.co.seoulit.his.pharmacyservice.inventory.entity.MedicationStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MedicationStockRepository extends JpaRepository<MedicationStock, String> {

    @Query("""
            SELECT s FROM MedicationStock s
            JOIN FETCH s.medicationLot l
            WHERE (:medicationId IS NULL OR l.medicationId = :medicationId)
              AND (:lotNo IS NULL OR l.lotNo = :lotNo)
              AND (:storageLocationId IS NULL OR s.storageLocationId = :storageLocationId)
              AND (:expirationFrom IS NULL OR l.expirationDt >= :expirationFrom)
              AND (:expirationTo IS NULL OR l.expirationDt <= :expirationTo)
            ORDER BY l.expirationDt ASC
            """)
    Page<MedicationStock> search(
            @Param("medicationId") String medicationId,
            @Param("lotNo") String lotNo,
            @Param("storageLocationId") String storageLocationId,
            @Param("expirationFrom") LocalDate expirationFrom,
            @Param("expirationTo") LocalDate expirationTo,
            Pageable pageable);

    @Query("SELECT s FROM MedicationStock s JOIN FETCH s.medicationLot WHERE s.medicationStockId = :medicationStockId")
    Optional<MedicationStock> findByIdWithLot(@Param("medicationStockId") String medicationStockId);

    Optional<MedicationStock> findByMedicationLot_MedicationLotIdAndStorageLocationId(String medicationLotId, String storageLocationId);

    /** 로트만 알고 보관위치는 모르는 경우(조제취소/반납 등)에 그 로트의 재고를 찾는다 */
    Optional<MedicationStock> findFirstByMedicationLot_MedicationLotIdOrderByStorageLocationIdAsc(String medicationLotId);

    /** 출고(HL2-8)용 — 재고가 남아있는 로트를 유효기간이 빠른 순(FEFO)으로 조회 */
    @Query("""
            SELECT s FROM MedicationStock s
            JOIN FETCH s.medicationLot l
            WHERE l.medicationId = :medicationId AND s.currentQty > 0
            ORDER BY l.expirationDt ASC
            """)
    List<MedicationStock> findAvailableByMedicationIdOrderByExpirationDtAsc(@Param("medicationId") String medicationId);

    /** 한 약품의 현재 전체 재고 합계(모든 로트/보관위치) — 처방전 상세에서 조제 가능 여부를 보여줄 때 쓴다 */
    @Query("""
            SELECT COALESCE(SUM(s.currentQty), 0) FROM MedicationStock s
            WHERE s.medicationLot.medicationId = :medicationId
            """)
    BigDecimal sumCurrentQtyByMedicationId(@Param("medicationId") String medicationId);

    /** 재고부족 목록 — 현재 수량이 기준값 이하인 재고를 수량이 적은 순으로 조회 */
    @Query("""
            SELECT s FROM MedicationStock s
            JOIN FETCH s.medicationLot l
            WHERE s.currentQty <= :threshold
            ORDER BY s.currentQty ASC
            """)
    List<MedicationStock> findLowStock(@Param("threshold") BigDecimal threshold);
}
