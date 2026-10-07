package kr.co.seoulit.his.pharmacyservice.prescription.dto;

/** 취소된 처방의 약품 항목(참고용). 약제는 처방 전체 단위로 취소하므로 항목별 처리에는 쓰지 않는다. */
public record PrescriptionCancelledItemEvent(
        String ediCode,
        String itemName
) {
}
