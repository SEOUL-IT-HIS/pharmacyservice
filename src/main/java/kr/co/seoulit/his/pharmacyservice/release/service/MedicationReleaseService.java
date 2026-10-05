package kr.co.seoulit.his.pharmacyservice.release.service;

import kr.co.seoulit.his.pharmacyservice.common.BusinessException;
import kr.co.seoulit.his.pharmacyservice.common.ErrorCode;
import kr.co.seoulit.his.pharmacyservice.prescription.entity.Dispensing;
import kr.co.seoulit.his.pharmacyservice.prescription.repository.DispensingRepository;
import kr.co.seoulit.his.pharmacyservice.prescription.repository.PrescriptionLinkRepository;
import kr.co.seoulit.his.pharmacyservice.release.dto.MedicationReleaseCreateRequest;
import kr.co.seoulit.his.pharmacyservice.release.dto.RecipientType;
import kr.co.seoulit.his.pharmacyservice.release.entity.MedicationRelease;
import kr.co.seoulit.his.pharmacyservice.release.entity.ReleaseCancel;
import kr.co.seoulit.his.pharmacyservice.release.repository.MedicationReleaseRepository;
import kr.co.seoulit.his.pharmacyservice.release.repository.ReleaseCancelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * 약품 불출(HL2-20 처리 / HL2-21 취소). "출고"(창고->병동)와 다른 개념으로,
 * 조제완료(Dispensing)된 약을 환자/보호자/병동 등 최종 수령자에게 전달하는 처리다.
 * 재고는 이미 조제완료 시점에 빠져나갔으므로 불출/불출취소 자체는 재고를 건드리지 않는다.
 */
@Service
public class MedicationReleaseService {

    /** 불출/불출취소 처리자 입력란이 아직 화면에 없어 임시로 고정값을 사용한다 */
    private static final String RELEASED_BY_PLACEHOLDER = "SYSTEM";
    private static final int CANCEL_REASON_MAX_BYTES = 20;

    private final PrescriptionLinkRepository prescriptionLinkRepository;
    private final DispensingRepository dispensingRepository;
    private final MedicationReleaseRepository medicationReleaseRepository;
    private final ReleaseCancelRepository releaseCancelRepository;

    public MedicationReleaseService(PrescriptionLinkRepository prescriptionLinkRepository,
                                     DispensingRepository dispensingRepository,
                                     MedicationReleaseRepository medicationReleaseRepository,
                                     ReleaseCancelRepository releaseCancelRepository) {
        this.prescriptionLinkRepository = prescriptionLinkRepository;
        this.dispensingRepository = dispensingRepository;
        this.medicationReleaseRepository = medicationReleaseRepository;
        this.releaseCancelRepository = releaseCancelRepository;
    }

    @Transactional
    public MedicationRelease create(MedicationReleaseCreateRequest request) {
        if (!RecipientType.isValid(request.recipientTypeCd())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        prescriptionLinkRepository.findById(request.prescriptionLinkId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRESCRIPTION_NOT_FOUND));

        Dispensing dispensing = dispensingRepository
                .findByPrescriptionLink_PrescriptionLinkIdAndDispenseStatusCd(
                        request.prescriptionLinkId(), Dispensing.STATUS_DISPENSED)
                .orElseThrow(() -> new BusinessException(ErrorCode.DISPENSING_NOT_FOUND));

        // DB 유니크 제약(DISPENSING_ID) 때문에 한 Dispensing은 평생 한 번만 불출 레코드를 가질 수
        // 있다 — 취소된 적이 있어도 그 Dispensing으로는 다시 불출을 만들 수 없다(새로 조제완료
        // 해야 재시도 가능).
        if (medicationReleaseRepository.existsByDispensing_DispensingId(dispensing.getDispensingId())) {
            throw new BusinessException(ErrorCode.RELEASE_ALREADY_EXISTS);
        }

        return medicationReleaseRepository.save(
                new MedicationRelease(dispensing, LocalDateTime.now(), request.recipientTypeCd()));
    }

    @Transactional
    public void cancel(String medicationReleaseId, String reason) {
        if (reason.getBytes(StandardCharsets.UTF_8).length > CANCEL_REASON_MAX_BYTES) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        MedicationRelease release = medicationReleaseRepository.findById(medicationReleaseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RELEASE_NOT_FOUND));

        if (!MedicationRelease.STATUS_RELEASED.equals(release.getReleaseStatusCd())) {
            throw new BusinessException(ErrorCode.RELEASE_ALREADY_CANCELLED);
        }

        releaseCancelRepository.save(new ReleaseCancel(release, LocalDateTime.now(), reason, RELEASED_BY_PLACEHOLDER));
        release.cancel();
    }
}
