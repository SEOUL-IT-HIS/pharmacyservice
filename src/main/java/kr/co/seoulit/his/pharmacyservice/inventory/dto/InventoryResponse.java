package kr.co.seoulit.his.pharmacyservice.inventory.dto;

import kr.co.seoulit.his.pharmacyservice.inventory.entity.MedicationLot;
import kr.co.seoulit.his.pharmacyservice.inventory.entity.MedicationStock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record InventoryResponse(
        String medicationStockId,
        String medicationLotId,
        String medicationId,
        String medicationName,
        String lotNo,
        LocalDate expirationDt,
        LocalDate manufactureDt,
        String unitCd,
        String storageLocationId,
        BigDecimal currentQty,
        LocalDateTime lastMovementAt
) {

    public static InventoryResponse from(MedicationStock stock, String medicationName) {
        MedicationLot lot = stock.getMedicationLot();
        return new InventoryResponse(
                stock.getMedicationStockId(),
                lot.getMedicationLotId(),
                lot.getMedicationId(),
                medicationName,
                lot.getLotNo(),
                lot.getExpirationDt(),
                lot.getManufactureDt(),
                lot.getUnitCd(),
                stock.getStorageLocationId(),
                stock.getCurrentQty(),
                stock.getLastMovementAt()
        );
    }
}
