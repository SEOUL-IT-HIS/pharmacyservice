package kr.co.seoulit.his.pharmacyservice.prescription.service;

import kr.co.seoulit.his.pharmacyservice.common.BusinessException;
import kr.co.seoulit.his.pharmacyservice.common.ErrorCode;
import kr.co.seoulit.his.pharmacyservice.medication.entity.Medication;
import kr.co.seoulit.his.pharmacyservice.medication.repository.MedicationRepository;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.DosageFormCode;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionCreatedEvent;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionDetailResponse;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionItemEvent;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionItemResponse;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionListResponse;
import kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionItemLink;
import kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionLink;
import kr.co.seoulit.his.pharmacyservice.prescription.repository.PrescriptionItemLinkRepository;
import kr.co.seoulit.his.pharmacyservice.prescription.repository.PrescriptionLinkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PrescriptionService {

    private static final Logger log = LoggerFactory.getLogger(PrescriptionService.class);

    private final PrescriptionLinkRepository prescriptionLinkRepository;
    private final PrescriptionItemLinkRepository prescriptionItemLinkRepository;
    private final MedicationRepository medicationRepository;

    public PrescriptionService(PrescriptionLinkRepository prescriptionLinkRepository,
                                PrescriptionItemLinkRepository prescriptionItemLinkRepository,
                                MedicationRepository medicationRepository) {
        this.prescriptionLinkRepository = prescriptionLinkRepository;
        this.prescriptionItemLinkRepository = prescriptionItemLinkRepository;
        this.medicationRepository = medicationRepository;
    }

    /**
     * 외래(GR2) 카프카 이벤트 소비 후 처방전 저장.
     * prescriptionId 기준으로 이미 저장된 이벤트면 재저장하지 않는다(멱등 처리 — 리밸런싱/재전송 대비).
     */
    @Transactional
    public void save(PrescriptionCreatedEvent event) {
        // 카프카로 들어오는 이벤트는 REST(@Valid)와 달리 Bean Validation이 자동 적용되지 않는다.
        // 외래(GR2) 쪽 실제 페이로드가 우리가 기대하는 스키마(특히 items)와 어긋나면 null로 들어올 수 있어
        // 여기서 직접 방어한다 — 이게 없으면 NPE로 터져서 리스너가 9번 재시도 후 그냥 버려진다.
        if (event.items() == null || event.items().isEmpty()) {
            log.error("처방전 이벤트에 items가 비어있음(외래 페이로드 스펙 불일치 의심). prescriptionId={}, event={}",
                    event.prescriptionId(), event);
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        // dosageFormCd는 자유형식이 아니라 DosageFormCode(TAB/IV/INJ) 3개 값만 허용한다.
        // 수납(billing) 쪽이 이 3개 대표 항목으로만 수가를 계산하기로 확정했기 때문에(21.10절),
        // 여기서 미리 걸러내지 않으면 잘못된 코드가 그대로 저장돼 나중에 수납 연동에서 조용히 깨진다.
        for (PrescriptionItemEvent itemEvent : event.items()) {
            if (!DosageFormCode.isValid(itemEvent.dosageFormCd())) {
                log.error("허용되지 않는 dosageFormCd. prescriptionId={}, ediCode={}, dosageFormCd={} (허용값: TAB/IV/INJ)",
                        event.prescriptionId(), itemEvent.ediCode(), itemEvent.dosageFormCd());
                throw new BusinessException(ErrorCode.INVALID_DOSAGE_FORM_CODE);
            }
        }

        if (prescriptionLinkRepository.findByPrescriptionId(event.prescriptionId()).isPresent()) {
            log.warn("이미 저장된 처방전 이벤트라 무시함. prescriptionId={}", event.prescriptionId());
            return;
        }

        PrescriptionLink link = prescriptionLinkRepository.save(new PrescriptionLink(
                event.prescriptionId(), event.patientId(), event.physicianId(),
                event.departmentId(), event.createdAt().toLocalDateTime()));

        for (PrescriptionItemEvent itemEvent : event.items()) {
            Medication medication = medicationRepository.findByEdiCodeContaining(itemEvent.ediCode())
                    .orElseThrow(() -> {
                        log.warn("약품 매핑 실패. itemName={}, ediCode={}", itemEvent.itemName(), itemEvent.ediCode());
                        return new BusinessException(ErrorCode.MEDICATION_NOT_FOUND);
                    });

            prescriptionItemLinkRepository.save(new PrescriptionItemLink(
                    link, String.valueOf(medication.getMedicationId()),
                    itemEvent.dosageQty(), itemEvent.dosageFormCd(),
                    itemEvent.frequency(), itemEvent.durationDays(), itemEvent.detailInfo()));
        }
    }

    public Page<PrescriptionListResponse> search(String prescriptionId, String patientId, String physicianId,
                                                  String departmentId, Pageable pageable) {
        return prescriptionLinkRepository
                .search(prescriptionId, patientId, physicianId, departmentId, pageable)
                .map(PrescriptionListResponse::from);
    }

    public PrescriptionDetailResponse getDetail(String prescriptionLinkId) {
        PrescriptionLink link = prescriptionLinkRepository.findById(prescriptionLinkId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRESCRIPTION_NOT_FOUND));

        List<PrescriptionItemResponse> items = prescriptionItemLinkRepository
                .findByPrescriptionLink_PrescriptionLinkId(prescriptionLinkId)
                .stream()
                .map(PrescriptionItemResponse::from)
                .toList();

        return PrescriptionDetailResponse.from(link, items);
    }
}
