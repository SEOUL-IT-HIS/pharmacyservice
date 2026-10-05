package kr.co.seoulit.his.pharmacyservice.controlleddrug.dto;

import kr.co.seoulit.his.pharmacyservice.controlleddrug.entity.ControlledDrugRecord;

import java.math.BigDecimal;
import java.util.List;

public record ControlledDrugRecordResponse(
        String controlledDrugRecordId,
        String inventoryMovementId,
        BigDecimal movementQty,
        String staffId,
        String controlledTxCd,
        List<String> witnessStaffIds
) {

    public static ControlledDrugRecordResponse from(ControlledDrugRecord record, List<String> witnessStaffIds) {
        return new ControlledDrugRecordResponse(
                record.getControlledDrugRecordId(),
                record.getInventoryMovement().getInventoryMovementId(),
                record.getInventoryMovement().getMovementQty(),
                record.getStaffId(),
                record.getControlledTxCd(),
                witnessStaffIds
        );
    }
}
