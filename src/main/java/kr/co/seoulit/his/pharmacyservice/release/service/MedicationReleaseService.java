package kr.co.seoulit.his.pharmacyservice.release.service;

import kr.co.seoulit.his.pharmacyservice.common.BusinessException;
import kr.co.seoulit.his.pharmacyservice.common.ErrorCode;
import kr.co.seoulit.his.pharmacyservice.medicationreturn.repository.MedicationReturnRepository;
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

    private static final int CANCEL_REASON_MAX_BYTES = 200;

    private final PrescriptionLinkRepository prescriptionLinkRepository;
    private final DispensingRepository dispensingRepository;
    private final MedicationReleaseRepository medicationReleaseRepository;
    private final ReleaseCancelRepository releaseCancelRepository;
    private final MedicationReturnRepository medicationReturnRepository;

    public MedicationReleaseService(PrescriptionLinkRepository prescriptionLinkRepository,
                                     DispensingRepository dispensingRepository,
                                     MedicationReleaseRepository medicationReleaseRepository,
                                     ReleaseCancelRepository releaseCancelRepository,
                                     MedicationReturnRepository medicationReturnRepository) {
        this.prescriptionLinkRepository = prescriptionLinkRepository;
        this.dispensingRepository = dispensingRepository;
        this.medicationReleaseRepository = medicationReleaseRepository;
        this.releaseCancelRepository = releaseCancelRepository;
        this.medicationReturnRepository = medicationReturnRepository;
    }

    @Transactional
    public MedicationRelease create(MedicationReleaseCreateRequest request) {
        if (!RecipientType.isValid(request.recipientTypeCd())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        // 받은 사람 정보 — 병동이면 받은 병동 직원, 보호자면 보호자 이름이 있어야 누구에게 줬는지 남는다.
        // 환자 본인이면 둘 다 필요 없으므로 값이 와도 저장하지 않는다.
        String receiverId = null;
        String guardianName = null;
        if (RecipientType.WARD.getCode().equals(request.recipientTypeCd())) {
            if (request.receiverId() == null || request.receiverId().isBlank()) {
                throw new BusinessException(ErrorCode.RELEASE_RECIPIENT_REQUIRED);
            }
            receiverId = request.receiverId().trim();
        } else if (RecipientType.GUARDIAN.getCode().equals(request.recipientTypeCd())) {
            if (request.guardianName() == null || request.guardianName().isBlank()) {
                throw new BusinessException(ErrorCode.RELEASE_RECIPIENT_REQUIRED);
            }
            guardianName = request.guardianName().trim();
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
                new MedicationRelease(dispensing, LocalDateTime.now(), request.recipientTypeCd(),
                        request.releasedById(), receiverId, guardianName));
    }

    @Transactional
    public void cancel(String medicationReleaseId, String reason, String actorId) {
        if (reason.getBytes(StandardCharsets.UTF_8).length > CANCEL_REASON_MAX_BYTES) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        MedicationRelease release = medicationReleaseRepository.findById(medicationReleaseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RELEASE_NOT_FOUND));

        if (!MedicationRelease.STATUS_RELEASED.equals(release.getReleaseStatusCd())) {
            throw new BusinessException(ErrorCode.RELEASE_ALREADY_CANCELLED);
        }

        // 이미 반납이 기록된 불출은 취소할 수 없다 — 반납 때 재고가 복구됐는데 취소(+이어지는 조제취소)에서 또
        // 복구하면 재고가 이중으로 늘어난다.
        if (medicationReturnRepository.existsByMedicationRelease_MedicationReleaseId(medicationReleaseId)) {
            throw new BusinessException(ErrorCode.RELEASE_CANCEL_BLOCKED_BY_RETURN);
        }

        releaseCancelRepository.save(new ReleaseCancel(release, LocalDateTime.now(), reason, actorId));
        release.cancel();
    }
}
