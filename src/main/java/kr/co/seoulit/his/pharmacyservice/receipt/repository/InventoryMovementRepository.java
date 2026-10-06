package kr.co.seoulit.his.pharmacyservice.receipt.repository;

import kr.co.seoulit.his.pharmacyservice.receipt.entity.InventoryMovement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, String> {

    /** 출고 조회(HL2-9) 화면용 — 특정 이동유형(예: 출고)의 이력을 로트/재고와 함께 최신순으로 조회 */
    @Query("""
            SELECT m FROM InventoryMovement m
            JOIN FETCH m.medicationStock s
            JOIN FETCH s.medicationLot
            WHERE m.stockTxTypeCd = :stockTxTypeCd
            ORDER BY m.movementAt DESC
            """)
    List<InventoryMovement> findAllByStockTxTypeCdOrderByMovementAtDesc(@Param("stockTxTypeCd") String stockTxTypeCd);

    /** 약품 상세 화면(품목 중심 워크스페이스)의 "최근 입출고 내역" 패널용 — 품목 하나의 입고/출고/폐기/조제 이력을 최신순으로 */
    @Query("""
            SELECT m FROM InventoryMovement m
            JOIN FETCH m.medicationStock s
            JOIN FETCH s.medicationLot l
            WHERE l.medicationId = :medicationId
            ORDER BY m.movementAt DESC
            """)
    List<InventoryMovement> findByMedicationIdOrderByMovementAtDesc(
            @Param("medicationId") String medicationId, Pageable pageable);

    /** 마약류(controlleddrug)가 입고/출고/폐기 처리 직후, 그 결과로 생긴 이력을 찾을 때 쓴다 */
    List<InventoryMovement> findBySourceFormId(String sourceFormId);
}
