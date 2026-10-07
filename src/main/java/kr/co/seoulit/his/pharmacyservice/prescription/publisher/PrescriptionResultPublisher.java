package kr.co.seoulit.his.pharmacyservice.prescription.publisher;

import kr.co.seoulit.his.pharmacyservice.prescription.dto.PharmacyOrderResultedEvent;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionResultEvent;
import kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 조제완료/거절 결과를 카프카로 발행한다. 응급/병동이 각자 독립된
 * Consumer Group으로 같은 토픽을 구독해서 동시에 처리한다(2026-09-11 합의).
 *
 * 발행 실패해도 예외를 던지지 않는다 — 조제완료/거절 자체(우리 DB 갱신)는 이미 끝난 뒤라,
 * 알림 발행 하나 실패했다고 그 처리를 되돌릴 이유는 없다. 대신 로그로 남겨서 추적한다.
 */
@Component
public class PrescriptionResultPublisher {

    private static final Logger log = LoggerFactory.getLogger(PrescriptionResultPublisher.class);
    private static final String SOURCE = "pharmacyservice";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public PrescriptionResultPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${pharmacy.kafka.topic.prescription-resulted}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(PrescriptionLink link) {
        PrescriptionResultEvent data = new PrescriptionResultEvent(
                link.getPrescriptionId(),
                link.getPatientId(),
                link.getPhysicianId(),
                link.getDepartmentId(),
                link.getStatus(),
                link.getRejectReason(),
                OffsetDateTime.now());

        PharmacyOrderResultedEvent envelope = new PharmacyOrderResultedEvent(
                UUID.randomUUID().toString(),
                "PHARMACY_ORDER_" + link.getStatus().name(),
                "v1",
                data.occurredAt(),
                SOURCE,
                data);

        // kafkaTemplate.send()는 비동기 실패를 whenComplete로 알려주지만, 브로커에 메타데이터를
        // 아예 못 가져오는 경우(브로커 다운 등)는 send() 호출 자체가 동기적으로 KafkaException을
        // 던진다. 주석에 적은 대로 "발행 실패가 조제완료/거절 처리 자체를 막으면 안 된다"는
        // 의도를 지키려면 이 동기 예외도 여기서 잡아야 한다 — 안 잡으면 트랜잭션이 전부
        // 롤백되어 DB에는 아무것도 안 남고 500만 내려간다.
        try {
            kafkaTemplate.send(topic, link.getPrescriptionId(), envelope)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("조제결과 이벤트 발행 실패. prescriptionId={}, status={}",
                                    link.getPrescriptionId(), link.getStatus(), ex);
                        }
                    });
        } catch (Exception ex) {
            log.error("조제결과 이벤트 발행 실패(동기). prescriptionId={}, status={}",
                    link.getPrescriptionId(), link.getStatus(), ex);
        }
    }
}
