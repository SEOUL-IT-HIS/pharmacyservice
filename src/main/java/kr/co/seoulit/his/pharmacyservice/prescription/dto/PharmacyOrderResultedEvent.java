package kr.co.seoulit.his.pharmacyservice.prescription.dto;

import java.time.OffsetDateTime;

/**
 * 약제(PHM)가 발행하는 카프카 이벤트 봉투.
 * 외래가 우리한테 보낼 때 쓰는 봉투(PharmacyOrderRequestedEvent)와 같은 형태로 맞췄다 —
 * 처방코어/응급/병동이 같은 파싱 로직을 재사용할 수 있게.
 */
public record PharmacyOrderResultedEvent(
        String eventId,
        String eventType,
        String version,
        OffsetDateTime occurredAt,
        String source,
        PrescriptionResultEvent data
) {
}
