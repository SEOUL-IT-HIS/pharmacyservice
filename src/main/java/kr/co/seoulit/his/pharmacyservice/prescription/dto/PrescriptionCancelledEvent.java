package kr.co.seoulit.his.pharmacyservice.prescription.dto;

import java.util.List;

/**
 * 외래(처방코어) -> 약제(PHM) 처방 취소 통보 페이로드. 처방 전체 단위이고 약제는 회신하지 않는 단방향이다.
 * 취소 일시는 봉투의 occurredAt을 쓴다. cancelledItems는 약제로 이미 보낸 항목 전체(참고용).
 */
public record PrescriptionCancelledEvent(
        String prescriptionId,
        String cancelReason,
        String cancelledBy,
        List<PrescriptionCancelledItemEvent> cancelledItems
) {
}
