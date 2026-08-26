package kr.co.seoulit.his.pharmacyservice.inventory.repository;

import kr.co.seoulit.his.pharmacyservice.inventory.entity.MedicationStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /** 출고(HL2-8)용 — 재고가 남아있는 로트를 유효기간이 빠른 순(FEFO)으로 조회 */
    @Query("""
            SELECT s FROM MedicationStock s
            JOIN FETCH s.medicationLot l
            WHERE l.medicationId = :medicationId AND s.currentQty > 0
            ORDER BY l.expirationDt ASC
            """)
    List<MedicationStock> findAvailableByMedicationIdOrderByExpirationDtAsc(@Param("medicationId") String medicationId);
}
