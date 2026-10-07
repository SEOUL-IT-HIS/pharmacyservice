package kr.co.seoulit.his.pharmacyservice.prescription.consumer;

import kr.co.seoulit.his.pharmacyservice.common.BusinessException;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PharmacyOrderCancelledEvent;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PharmacyOrderRequestedEvent;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionCreatedEvent;
import kr.co.seoulit.his.pharmacyservice.prescription.service.PrescriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 외래(GR2)가 처방전을 작성하면 발행하는 카프카 이벤트를 소비해서 약제(PHM) 쪽에 저장한다.
 * 조회(PrescriptionController)는 REST GET, 저장은 이 컨슈머 — 두 경로가 분리되어 있다.
 *
 * 외래는 자체 공통 이벤트 봉투(eventId/eventType/version/occurredAt/source)로 감싸서 발행하고,
 * 약제 도메인 데이터는 그 안의 data에 중첩되어 온다(2026-09-03 외래팀과 확인, 팀 간 합의로 약제
 * 쪽이 이 구조를 그대로 받기로 함 — 외래의 다른 토픽들과 컨벤션을 맞추기 위함).
 * 그래서 리스너는 {@link PharmacyOrderRequestedEvent}를 받고, 실제 저장 로직에는 그 안의
 * {@code data()}(={@link PrescriptionCreatedEvent})만 넘긴다.
 *
 * 참고: 지금은 매핑 실패(BusinessException) 시 로그만 남기고 넘어간다. 재처리가 꼭 필요해지면
 * DLQ(Dead Letter Topic) + DefaultErrorHandler 도입을 검토할 것 — 지금 범위에선 과함.
 */
@Component
public class PrescriptionEventConsumer {

    // 취소 토픽은 구조가 달라서, 기본 역직렬화 타입(요청 이벤트)이 아니라 이 타입으로 읽도록 리스너에 직접 지정한다.
    private static final String CANCELLED_EVENT_TYPE_PROPERTY =
            "spring.json.value.default.type=kr.co.seoulit.his.pharmacyservice.prescription.dto.PharmacyOrderCancelledEvent";

    private static final Logger log = LoggerFactory.getLogger(PrescriptionEventConsumer.class);

    private final PrescriptionService prescriptionService;

    public PrescriptionEventConsumer(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @KafkaListener(topics = "${pharmacy.kafka.topic.prescription-created}")
    public void onPrescriptionCreated(PharmacyOrderRequestedEvent envelope) {
        if (envelope == null) {
            log.error("처방전 이벤트 역직렬화 실패로 null이 수신됨. 메시지 스펙을 확인할 것.");
            return;
        }
        PrescriptionCreatedEvent event = envelope.data();
        if (event == null) {
            log.error("처방전 이벤트 봉투는 받았으나 data가 비어있음. eventId={}, eventType={}",
                    envelope.eventId(), envelope.eventType());
            return;
        }
        try {
            prescriptionService.save(event);
        } catch (BusinessException e) {
            log.error("처방전 이벤트 저장 실패. eventId={}, prescriptionId={}, reason={}",
                    envelope.eventId(), event.prescriptionId(), e.getMessage(), e);
        }
    }

    /** 처방코어의 취소 통보. 회신/결과 이벤트 없이 약제 DB만 갱신한다(단방향). */
    @KafkaListener(topics = "${pharmacy.kafka.topic.prescription-cancelled}", properties = CANCELLED_EVENT_TYPE_PROPERTY)
    public void onPrescriptionCancelled(PharmacyOrderCancelledEvent envelope) {
        if (envelope == null || envelope.data() == null || envelope.data().prescriptionId() == null) {
            log.error("처방 취소 이벤트 역직렬화 실패 또는 data/prescriptionId 누락. eventId={}",
                    envelope == null ? null : envelope.eventId());
            return;
        }
        try {
            prescriptionService.cancelByPrescription(envelope.data());
        } catch (BusinessException e) {
            log.error("처방 취소 이벤트 처리 실패. eventId={}, prescriptionId={}, reason={}",
                    envelope.eventId(), envelope.data().prescriptionId(), e.getMessage(), e);
        }
    }
}
