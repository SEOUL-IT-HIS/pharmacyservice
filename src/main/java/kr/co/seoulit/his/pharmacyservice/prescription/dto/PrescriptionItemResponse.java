package kr.co.seoulit.his.pharmacyservice.prescription.dto;

import kr.co.seoulit.his.pharmacyservice.prescription.entity.DispensingItem;
import kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionItemLink;

import java.math.BigDecimal;

public record PrescriptionItemResponse(
        String prescriptionItemLinkId,
        String medicationId,
        BigDecimal dosageQty,
        String dosageFormCd,
        String frequency,
        String durationDays,
        String detailInfo,
        // 조제완료(활성 Dispensing) 때 이 처방항목이 실제로 어느 DispensingItem으로 기록됐는지.
        // 반납 처리 화면에서 dispensingItemId가 필요해서 같이 내려준다. 아직 조제 전이면 null.
        String dispensingItemId,
        BigDecimal dispensedQty
) {

    public static PrescriptionItemResponse from(PrescriptionItemLink item) {
        return from(item, null);
    }

    public static PrescriptionItemResponse from(PrescriptionItemLink item, DispensingItem dispensingItem) {
        return new PrescriptionItemResponse(
                item.getPrescriptionItemLinkId(),
                item.getMedicationId(),
                item.getDosageQty(),
                item.getDosageFormCd(),
                item.getFrequency(),
                item.getDurationDays(),
                item.getDetailInfo(),
                dispensingItem == null ? null : dispensingItem.getDispensingItemId(),
                dispensingItem == null ? null : dispensingItem.getDispensedQty()
        );
    }
}
