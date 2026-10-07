package kr.co.seoulit.his.pharmacyservice.prescription.dto;

import java.time.OffsetDateTime;

/** 처방코어가 취소 시 발행하는 이벤트 봉투. 요청 이벤트(PharmacyOrderRequestedEvent)와 같은 구조다. */
public record PharmacyOrderCancelledEvent(
        String eventId,
        String eventType,
        String version,
        OffsetDateTime occurredAt,
        String source,
        PrescriptionCancelledEvent data
) {
}
