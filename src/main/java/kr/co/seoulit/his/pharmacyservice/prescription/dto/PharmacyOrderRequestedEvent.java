package kr.co.seoulit.his.pharmacyservice.prescription.dto;

import jakarta.validation.Valid;

import java.time.OffsetDateTime;

/**
 * 외래(GR2)가 카프카에 실제로 발행하는 이벤트 봉투(envelope) 구조.
 * 외래 쪽 공통 이벤트 포맷(eventId/eventType/version/occurredAt/source)으로 감싸져 있고,
 * 약제(PHM) 도메인 데이터는 {@code data} 안에 중첩되어 온다.
 *
 * 외래팀의 실제 발행 포맷(2026-09-03 확인)에 맞춰 작성. "약제가 이 구조를 그대로 받는다"로 팀 간 합의됨
 * (외래가 다른 토픽에도 동일 컨벤션을 쓰고 있어, 영향 범위가 작은 약제 쪽이 맞추는 쪽으로 결정).
 * (eventId/eventType/version/source는 지금은 안 쓰지만, 나중에 이벤트 종류 분기나 트레이싱에 필요해지면 활용 가능)
 */
public record PharmacyOrderRequestedEvent(
        String eventId,
        String eventType,
        String version,
        OffsetDateTime occurredAt,
        String source,
        @Valid PrescriptionCreatedEvent data
) {
}
