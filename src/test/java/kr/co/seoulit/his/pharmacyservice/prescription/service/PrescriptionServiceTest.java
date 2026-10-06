package kr.co.seoulit.his.pharmacyservice.prescription.service;

import kr.co.seoulit.his.pharmacyservice.common.BusinessException;
import kr.co.seoulit.his.pharmacyservice.common.ErrorCode;
import kr.co.seoulit.his.pharmacyservice.medication.entity.Medication;
import kr.co.seoulit.his.pharmacyservice.medication.repository.MedicationRepository;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionCreatedEvent;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionDetailResponse;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionItemEvent;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionListResponse;
import kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionItemLink;
import kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionLink;
import kr.co.seoulit.his.pharmacyservice.prescription.repository.DispensingCancelRepository;
import kr.co.seoulit.his.pharmacyservice.prescription.repository.DispensingItemRepository;
import kr.co.seoulit.his.pharmacyservice.prescription.repository.DispensingRepository;
import kr.co.seoulit.his.pharmacyservice.prescription.repository.PrescriptionItemLinkRepository;
import kr.co.seoulit.his.pharmacyservice.prescription.repository.PrescriptionLinkRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceTest {

    @Mock
    private PrescriptionLinkRepository prescriptionLinkRepository;
    @Mock
    private PrescriptionItemLinkRepository prescriptionItemLinkRepository;
    @Mock
    private MedicationRepository medicationRepository;
    @Mock
    private kr.co.seoulit.his.pharmacyservice.prescription.publisher.PrescriptionResultPublisher prescriptionResultPublisher;
    @Mock
    private DispensingRepository dispensingRepository;
    @Mock
    private DispensingItemRepository dispensingItemRepository;
    @Mock
    private DispensingCancelRepository dispensingCancelRepository;
    @Mock
    private kr.co.seoulit.his.pharmacyservice.inventory.service.StockMovementService stockMovementService;
    @Mock
    private kr.co.seoulit.his.pharmacyservice.release.repository.MedicationReleaseRepository medicationReleaseRepository;

    @InjectMocks
    private PrescriptionService prescriptionService;

    private Medication newMedication(long medicationId, String ediCode) {
        Medication medication = new Medication();
        medication.setMedicationId(medicationId);
        medication.setMedicationName("타이레놀정500mg");
        medication.setEdiCode(ediCode);
        return medication;
    }

    private PrescriptionItemEvent newItemEvent(String ediCode) {
        // dosageFormCd는 admin 공통코드 DOSAGE_FORM_CD 값("01"=알약/캡슐)이다. 2026-10-06 이전엔
        // enum 이름(TAB)과 그대로 비교했었는데, 실제 외래/입원/응급이 보내는 값은 숫자코드라
        // DosageFormCode가 code 필드 비교로 바뀌었다 — 테스트 픽스처도 같이 맞춘다.
        return new PrescriptionItemEvent(
                ediCode, "타이레놀정500mg", BigDecimal.valueOf(500), "01",
                "1일 3회", "5일", "식후 30분 복용");
    }

    private PrescriptionCreatedEvent newEvent(String prescriptionId, List<PrescriptionItemEvent> items) {
        return new PrescriptionCreatedEvent(
                prescriptionId, "PATIENT-001", "PHYSICIAN-001", "DEPARTMENT-001",
                OffsetDateTime.parse("2026-07-16T09:00:00+09:00"), items);
    }

    @Test
    void save_persistsLinkAndItems_whenNew() {
        PrescriptionCreatedEvent event = newEvent("PRESCRIPTION-100", List.of(newItemEvent("EDI-001")));
        when(prescriptionLinkRepository.findByPrescriptionId("PRESCRIPTION-100")).thenReturn(Optional.empty());
        when(prescriptionLinkRepository.save(any(PrescriptionLink.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(medicationRepository.findByEdiCodeContaining("EDI-001"))
                .thenReturn(Optional.of(newMedication(1L, "EDI-001")));

        prescriptionService.save(event);

        ArgumentCaptor<PrescriptionLink> linkCaptor = ArgumentCaptor.forClass(PrescriptionLink.class);
        verify(prescriptionLinkRepository).save(linkCaptor.capture());
        assertThat(linkCaptor.getValue().getPrescriptionId()).isEqualTo("PRESCRIPTION-100");

        ArgumentCaptor<PrescriptionItemLink> itemCaptor = ArgumentCaptor.forClass(PrescriptionItemLink.class);
        verify(prescriptionItemLinkRepository).save(itemCaptor.capture());
        PrescriptionItemLink savedItem = itemCaptor.getValue();
        assertThat(savedItem.getMedicationId()).isEqualTo("1");
        assertThat(savedItem.getDosageQty()).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(savedItem.getDosageFormCd()).isEqualTo("01");
        assertThat(savedItem.getFrequency()).isEqualTo("1일 3회");
        assertThat(savedItem.getDurationDays()).isEqualTo("5일");
        assertThat(savedItem.getDetailInfo()).isEqualTo("식후 30분 복용");
    }

    @Test
    void save_skips_whenPrescriptionIdAlreadyExists() {
        PrescriptionCreatedEvent event = newEvent("PRESCRIPTION-DUP", List.of(newItemEvent("EDI-001")));
        when(prescriptionLinkRepository.findByPrescriptionId("PRESCRIPTION-DUP"))
                .thenReturn(Optional.of(newPrescriptionLink()));

        prescriptionService.save(event);

        verify(prescriptionLinkRepository, never()).save(any());
        verify(prescriptionItemLinkRepository, never()).save(any());
    }

    @Test
    void save_throwsInvalidDosageFormCode_whenNotOneOfTabIvInj() {
        PrescriptionItemEvent invalidItem = new PrescriptionItemEvent(
                "EDI-001", "타이레놀정500mg", BigDecimal.valueOf(500), "SYR",
                "1일 3회", "5일", "식후 30분 복용");
        PrescriptionCreatedEvent event = newEvent("PRESCRIPTION-300", List.of(invalidItem));

        assertThatThrownBy(() -> prescriptionService.save(event))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_DOSAGE_FORM_CODE);

        // 코드 검증은 저장 전에 걸러내야 한다 — 잘못된 값이면 링크조차 만들어지면 안 됨
        verify(prescriptionLinkRepository, never()).save(any());
        verify(prescriptionItemLinkRepository, never()).save(any());
    }

    @Test
    void save_throwsMedicationNotFound_whenEdiCodeUnmatched() {
        PrescriptionCreatedEvent event = newEvent("PRESCRIPTION-200", List.of(newItemEvent("UNKNOWN-EDI")));
        when(prescriptionLinkRepository.findByPrescriptionId("PRESCRIPTION-200")).thenReturn(Optional.empty());
        when(prescriptionLinkRepository.save(any(PrescriptionLink.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(medicationRepository.findByEdiCodeContaining("UNKNOWN-EDI")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> prescriptionService.save(event))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.MEDICATION_NOT_FOUND);

        verify(prescriptionItemLinkRepository, never()).save(any());
    }

    private PrescriptionLink newPrescriptionLink() {
        // PrescriptionLink 생성자는 UUID를 자체 생성한다(외래는 링크ID를 안 보내므로).
        // 예전엔 no-arg 생성자 + ReflectionTestUtils로 필드를 직접 꽂았는데, save() 구현하면서
        // no-arg 생성자를 protected로 바꿔서(다른 패키지에서 접근 불가) 이 방식은 컴파일이 안 된다.
        return new PrescriptionLink(
                "PRESCRIPTION-001", "PATIENT-001", "PHYSICIAN-001", "DEPARTMENT-001",
                LocalDateTime.of(2026, 7, 16, 9, 0));
    }

    @Test
    void search_returnsMappedPage() {
        PrescriptionLink link = newPrescriptionLink();
        Pageable pageable = PageRequest.of(0, 20);
        when(prescriptionLinkRepository.search(eq("PRESCRIPTION-001"), any(), any(), any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(link), pageable, 1));

        Page<PrescriptionListResponse> result = prescriptionService.search(
                "PRESCRIPTION-001", null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).prescriptionId()).isEqualTo("PRESCRIPTION-001");
    }

    @Test
    void getDetail_returnsResponseWithItems_whenExists() {
        PrescriptionLink link = newPrescriptionLink();
        when(prescriptionLinkRepository.findById(link.getPrescriptionLinkId())).thenReturn(Optional.of(link));
        when(prescriptionItemLinkRepository.findByPrescriptionLink_PrescriptionLinkId(link.getPrescriptionLinkId()))
                .thenReturn(List.of());
        when(dispensingRepository.findByPrescriptionLink_PrescriptionLinkIdAndDispenseStatusCd(
                link.getPrescriptionLinkId(),
                kr.co.seoulit.his.pharmacyservice.prescription.entity.Dispensing.STATUS_DISPENSED))
                .thenReturn(Optional.empty());

        PrescriptionDetailResponse response = prescriptionService.getDetail(link.getPrescriptionLinkId());

        assertThat(response.prescriptionLinkId()).isEqualTo(link.getPrescriptionLinkId());
        assertThat(response.items()).isEmpty();
        assertThat(response.release()).isNull();
    }

    @Test
    void getDetail_throwsNotFound_whenMissing() {
        when(prescriptionLinkRepository.findById("missing-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> prescriptionService.getDetail("missing-id"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRESCRIPTION_NOT_FOUND);
    }

    @Test
    void dispense_setsStatusAndPublishes_whenReceived() {
        PrescriptionLink link = newPrescriptionLink();
        when(prescriptionLinkRepository.findById(link.getPrescriptionLinkId())).thenReturn(Optional.of(link));
        when(dispensingRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        // 이 처방은 조제항목이 없는 상태로 테스트하므로(= save()를 거치지 않은 newPrescriptionLink()),
        // 재고 차감 루프는 비어서 그냥 지나간다.
        when(prescriptionItemLinkRepository.findByPrescriptionLink_PrescriptionLinkId(link.getPrescriptionLinkId()))
                .thenReturn(List.of());

        prescriptionService.dispense(link.getPrescriptionLinkId());

        assertThat(link.getStatus()).isEqualTo(kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionStatus.DISPENSED);
        verify(prescriptionResultPublisher).publish(link);
    }

    @Test
    void dispense_throwsAlreadyProcessed_whenNotReceived() {
        PrescriptionLink link = newPrescriptionLink();
        link.dispense();
        when(prescriptionLinkRepository.findById(link.getPrescriptionLinkId())).thenReturn(Optional.of(link));

        assertThatThrownBy(() -> prescriptionService.dispense(link.getPrescriptionLinkId()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRESCRIPTION_ALREADY_PROCESSED);

        verify(prescriptionResultPublisher, never()).publish(any());
    }

    @Test
    void reject_setsReasonAndPublishes_whenReceived() {
        PrescriptionLink link = newPrescriptionLink();
        when(prescriptionLinkRepository.findById(link.getPrescriptionLinkId())).thenReturn(Optional.of(link));

        prescriptionService.reject(link.getPrescriptionLinkId(), "재고 없음");

        assertThat(link.getStatus()).isEqualTo(kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionStatus.REJECTED);
        assertThat(link.getRejectReason()).isEqualTo("재고 없음");
        verify(prescriptionResultPublisher).publish(link);
    }
}
