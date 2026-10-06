package kr.co.seoulit.his.pharmacyservice.inventory.dto;

import kr.co.seoulit.his.pharmacyservice.receipt.entity.InventoryMovement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 약품 상세(품목 중심 워크스페이스)의 "최근 입출고 내역" 패널용 */
public record InventoryMovementResponse(
        String inventoryMovementId,
        /** PHM_STOCK_TX_TYPE 공통코드 — 01=입고, 02=출고, 03=폐기, 04=조제, 05=조제취소, 06=반납 */
        String stockTxTypeCd,
        BigDecimal movementQty,
        BigDecimal beforeQty,
        BigDecimal afterQty,
        String lotNo,
        String storageLocationId,
        String sourceFormTypeCd,
        LocalDateTime movementAt
) {

    public static InventoryMovementResponse from(InventoryMovement movement) {
        return new InventoryMovementResponse(
                movement.getInventoryMovementId(),
                movement.getStockTxTypeCd(),
                movement.getMovementQty(),
                movement.getBeforeQty(),
                movement.getAfterQty(),
                movement.getMedicationStock().getMedicationLot().getLotNo(),
                movement.getMedicationStock().getStorageLocationId(),
                movement.getSourceFormTypeCd(),
                movement.getMovementAt()
        );
    }
}
