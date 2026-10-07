package kr.co.seoulit.his.pharmacyservice.prescription.consumer;

import kr.co.seoulit.his.pharmacyservice.common.BusinessException;
import kr.co.seoulit.his.pharmacyservice.common.ErrorCode;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PharmacyOrderCancelledEvent;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PharmacyOrderRequestedEvent;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionCancelledEvent;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionCreatedEvent;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionItemEvent;
import kr.co.seoulit.his.pharmacyservice.prescription.service.PrescriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * 카프카 리스너 자체는 스프링 부트 컨텍스트/브로커가 있어야 end-to-end로 검증되지만,
 * 여기선 "메시지를 받으면 서비스로 위임한다"와 "저장 실패(BusinessException)가 컨슈머 스레드를
 * 죽이지 않는다" 두 가지 계약만 단위 테스트로 고정해둔다.
 *
 * 외래는 실제로 이벤트 봉투(eventId/eventType/version/occurredAt/source)로 감싸서 보내고
 * 약제 도메인 데이터는 그 안의 data에 중첩되어 오므로, 테스트도 그 구조를 그대로 흉내낸다.
 */
@ExtendWith(MockitoExtension.class)
class PrescriptionEventConsumerTest {

    @Mock
    private PrescriptionService prescriptionService;

    @InjectMocks
    private PrescriptionEventConsumer consumer;

    private PrescriptionCreatedEvent newData() {
        PrescriptionItemEvent item = new PrescriptionItemEvent(
                "EDI-001", "타이레놀정500mg", BigDecimal.valueOf(500), "TAB",
                "1일 3회", "5일", "식후 30분 복용");
        return new PrescriptionCreatedEvent(
                "PRESCRIPTION-100", "PATIENT-001", "PHYSICIAN-001", "DEPARTMENT-001",
                OffsetDateTime.parse("2026-07-16T09:00:00+09:00"), null, null, null, List.of(item));
    }

    private PharmacyOrderRequestedEvent newEnvelope(PrescriptionCreatedEvent data) {
        return new PharmacyOrderRequestedEvent(
                "EVENT-001", "PharmacyOrderRequested", "1.0",
                OffsetDateTime.parse("2026-07-16T09:00:00+09:00"), "OPD", data);
    }

    @Test
    void onPrescriptionCreated_delegatesToService() {
        PrescriptionCreatedEvent data = newData();
        PharmacyOrderRequestedEvent envelope = newEnvelope(data);

        consumer.onPrescriptionCreated(envelope);

        verify(prescriptionService).save(data);
    }

    @Test
    void onPrescriptionCreated_doesNotPropagate_whenServiceThrowsBusinessException() {
        PrescriptionCreatedEvent data = newData();
        PharmacyOrderRequestedEvent envelope = newEnvelope(data);
        doThrow(new BusinessException(ErrorCode.MEDICATION_NOT_FOUND))
                .when(prescriptionService).save(data);

        // 여기서 예외가 밖으로 안 새는 게 핵심 — 새면 컨슈머 스레드가 죽어서 이후 메시지를 못 받는다.
        assertThatCode(() -> consumer.onPrescriptionCreated(envelope)).doesNotThrowAnyException();
    }

    @Test
    void onPrescriptionCreated_doesNothing_whenEnvelopeIsNull() {
        consumer.onPrescriptionCreated(null);

        verifyNoInteractions(prescriptionService);
    }

    @Test
    void onPrescriptionCreated_doesNothing_whenEnvelopeDataIsNull() {
        consumer.onPrescriptionCreated(newEnvelope(null));

        verifyNoInteractions(prescriptionService);
    }

    private PharmacyOrderCancelledEvent newCancelEnvelope(PrescriptionCancelledEvent data) {
        return new PharmacyOrderCancelledEvent(
                "EVENT-002", "PharmacyOrderCancelled", "v1",
                OffsetDateTime.parse("2026-07-16T10:00:00+09:00"), "OPD", data);
    }

    @Test
    void onPrescriptionCancelled_delegatesToService() {
        PrescriptionCancelledEvent data = new PrescriptionCancelledEvent(
                "PRESCRIPTION-100", "환자 요청", "DOCTOR-01", List.of());

        consumer.onPrescriptionCancelled(newCancelEnvelope(data));

        verify(prescriptionService).cancelByPrescription(data);
    }

    @Test
    void onPrescriptionCancelled_doesNotPropagate_whenServiceThrowsBusinessException() {
        PrescriptionCancelledEvent data = new PrescriptionCancelledEvent(
                "PRESCRIPTION-100", "환자 요청", "DOCTOR-01", List.of());
        doThrow(new BusinessException(ErrorCode.DISPENSING_NOT_FOUND))
                .when(prescriptionService).cancelByPrescription(data);

        assertThatCode(() -> consumer.onPrescriptionCancelled(newCancelEnvelope(data))).doesNotThrowAnyException();
    }

    @Test
    void onPrescriptionCancelled_doesNothing_whenEnvelopeOrDataInvalid() {
        consumer.onPrescriptionCancelled(null);
        consumer.onPrescriptionCancelled(newCancelEnvelope(null));
        consumer.onPrescriptionCancelled(newCancelEnvelope(
                new PrescriptionCancelledEvent(null, "사유", "DOCTOR-01", List.of())));

        verifyNoInteractions(prescriptionService);
    }
}
