package kr.co.seoulit.his.pharmacyservice.prescription.dto;

import kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionItemLink;

import java.math.BigDecimal;

public record PrescriptionItemResponse(
        String prescriptionItemLinkId,
        String medicationId,
        BigDecimal dosageQty,
        String dosageFormCd
) {

    public static PrescriptionItemResponse from(PrescriptionItemLink item) {
        return new PrescriptionItemResponse(
                item.getPrescriptionItemLinkId(),
                item.getMedicationId(),
                item.getDosageQty(),
                item.getDosageFormCd()
        );
    }
}
