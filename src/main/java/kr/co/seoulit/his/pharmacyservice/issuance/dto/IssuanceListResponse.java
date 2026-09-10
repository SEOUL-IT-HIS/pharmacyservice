package kr.co.seoulit.his.pharmacyservice.issuance.dto;

import kr.co.seoulit.his.pharmacyservice.inventory.entity.MedicationLot;
import kr.co.seoulit.his.pharmacyservice.inventory.entity.MedicationStock;
import kr.co.seoulit.his.pharmacyservice.receipt.entity.InventoryMovement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 출고 조회(HL2-9) 화면용 — 출고 이력(InventoryMovement, STOCK_TX_TYPE_CD='02')을 약품 단위로 평탄화해서 보여준다. */
public record IssuanceListResponse(
        String medicationId,
        String medicationName,
        String lotNo,
        String storageLocationId,
        BigDecimal quantity,
        LocalDateTime issuedAt
) {

    public static IssuanceListResponse from(InventoryMovement movement, String medicationName) {
        MedicationStock stock = movement.getMedicationStock();
        MedicationLot lot = stock.getMedicationLot();
        return new IssuanceListResponse(
                lot.getMedicationId(),
                medicationName,
                lot.getLotNo(),
                stock.getStorageLocationId(),
                movement.getMovementQty(),
                movement.getMovementAt()
        );
    }
}
