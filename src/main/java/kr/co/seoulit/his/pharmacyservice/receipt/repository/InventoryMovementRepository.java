package kr.co.seoulit.his.pharmacyservice.receipt.repository;

import kr.co.seoulit.his.pharmacyservice.receipt.entity.InventoryMovement;
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
}
