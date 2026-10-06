package kr.co.seoulit.his.pharmacyservice.prescription.dto;

import kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionItemLink;

import java.math.BigDecimal;
import java.util.List;

public record PrescriptionItemResponse(
        String prescriptionItemLinkId,
        String medicationId,
        // 약품 마스터에서 가져온 표시용 값 — 약제 DB 안의 데이터라 응답에 같이 담아 준다
        String medicationName,
        String ediCode,
        BigDecimal dosageQty,
        String dosageFormCd,
        String frequency,
        String durationDays,
        String detailInfo,
        // 조제하면 차감될 예정 수량(1회량 x 횟수 x 일수). 조제완료 후에는 실제 조제 수량(dispensedQty)이 기준이다.
        BigDecimal expectedQty,
        // true면 횟수/일수를 숫자 하나로 못 읽어 그 구간을 1로 계산했다는 뜻 — 사람이 확인해야 한다.
        boolean qtyAmbiguous,
        // 이 약품의 현재 전체 재고 합계(모든 로트/보관위치)
        BigDecimal availableQty,
        // 조제완료(활성 Dispensing) 때 실제 조제된 총 수량. 아직 조제 전이면 null.
        BigDecimal dispensedQty,
        // 조제에 사용된 로트별 내역(반납 처리 단위). 조제 전이면 빈 목록.
        List<DispensingLotResponse> dispensingLots
) {

    public static PrescriptionItemResponse from(PrescriptionItemLink item, String medicationName, String ediCode,
                                                 BigDecimal expectedQty, boolean qtyAmbiguous, BigDecimal availableQty,
                                                 List<DispensingLotResponse> dispensingLots) {
        BigDecimal dispensedQty = dispensingLots.isEmpty()
                ? null
                : dispensingLots.stream().map(DispensingLotResponse::dispensedQty).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PrescriptionItemResponse(
                item.getPrescriptionItemLinkId(),
                item.getMedicationId(),
                medicationName,
                ediCode,
                item.getDosageQty(),
                item.getDosageFormCd(),
                item.getFrequency(),
                item.getDurationDays(),
                item.getDetailInfo(),
                expectedQty,
                qtyAmbiguous,
                availableQty,
                dispensedQty,
                dispensingLots
        );
    }
}
