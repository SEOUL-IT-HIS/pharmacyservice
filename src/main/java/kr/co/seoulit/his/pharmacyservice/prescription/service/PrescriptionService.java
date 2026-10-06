package kr.co.seoulit.his.pharmacyservice.prescription.service;

import kr.co.seoulit.his.pharmacyservice.common.BusinessException;
import kr.co.seoulit.his.pharmacyservice.common.ErrorCode;
import kr.co.seoulit.his.pharmacyservice.inventory.entity.MedicationLot;
import kr.co.seoulit.his.pharmacyservice.inventory.service.StockMovementService;
import kr.co.seoulit.his.pharmacyservice.medication.entity.Medication;
import kr.co.seoulit.his.pharmacyservice.medication.repository.MedicationRepository;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.DosageFormCode;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionCreatedEvent;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionDetailResponse;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionItemEvent;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionItemResponse;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionListResponse;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.ReleaseInfoResponse;
import kr.co.seoulit.his.pharmacyservice.prescription.entity.Dispensing;
import kr.co.seoulit.his.pharmacyservice.prescription.entity.DispensingCancel;
import kr.co.seoulit.his.pharmacyservice.prescription.entity.DispensingItem;
import kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionItemLink;
import kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionLink;
import kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionStatus;
import kr.co.seoulit.his.pharmacyservice.prescription.publisher.PrescriptionResultPublisher;
import kr.co.seoulit.his.pharmacyservice.prescription.repository.DispensingCancelRepository;
import kr.co.seoulit.his.pharmacyservice.prescription.repository.DispensingItemRepository;
import kr.co.seoulit.his.pharmacyservice.prescription.repository.DispensingRepository;
import kr.co.seoulit.his.pharmacyservice.prescription.repository.PrescriptionItemLinkRepository;
import kr.co.seoulit.his.pharmacyservice.prescription.repository.PrescriptionLinkRepository;
import kr.co.seoulit.his.pharmacyservice.release.entity.MedicationRelease;
import kr.co.seoulit.his.pharmacyservice.release.repository.MedicationReleaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class PrescriptionService {

    private static final Logger log = LoggerFactory.getLogger(PrescriptionService.class);

    private static final String SOURCE_FORM_TYPE_DISPENSING = "DISPENSING";
    private static final String SOURCE_FORM_TYPE_DISPENSING_CANCEL = "DISPENSING_CANCEL";
    /** 조제완료/조제취소 처리자 입력란이 아직 화면에 없어 임시로 고정값을 사용한다 (출고/폐기와 동일한 단순화) */
    private static final String DISPENSED_BY_PLACEHOLDER = "SYSTEM";
    /** CANCEL_REASON_CD 컬럼이 20바이트라, 그 안에 들어가는 사유만 저장하고 넘으면 400으로 막는다 */
    private static final int CANCEL_REASON_MAX_BYTES = 20;

    private final PrescriptionLinkRepository prescriptionLinkRepository;
    private final PrescriptionItemLinkRepository prescriptionItemLinkRepository;
    private final MedicationRepository medicationRepository;
    private final PrescriptionResultPublisher prescriptionResultPublisher;
    private final DispensingRepository dispensingRepository;
    private final DispensingItemRepository dispensingItemRepository;
    private final DispensingCancelRepository dispensingCancelRepository;
    private final StockMovementService stockMovementService;
    private final MedicationReleaseRepository medicationReleaseRepository;

    public PrescriptionService(PrescriptionLinkRepository prescriptionLinkRepository,
                                PrescriptionItemLinkRepository prescriptionItemLinkRepository,
                                MedicationRepository medicationRepository,
                                PrescriptionResultPublisher prescriptionResultPublisher,
                                DispensingRepository dispensingRepository,
                                DispensingItemRepository dispensingItemRepository,
                                DispensingCancelRepository dispensingCancelRepository,
                                StockMovementService stockMovementService,
                                MedicationReleaseRepository medicationReleaseRepository) {
        this.prescriptionLinkRepository = prescriptionLinkRepository;
        this.prescriptionItemLinkRepository = prescriptionItemLinkRepository;
        this.medicationRepository = medicationRepository;
        this.prescriptionResultPublisher = prescriptionResultPublisher;
        this.dispensingRepository = dispensingRepository;
        this.dispensingItemRepository = dispensingItemRepository;
        this.dispensingCancelRepository = dispensingCancelRepository;
        this.stockMovementService = stockMovementService;
        this.medicationReleaseRepository = medicationReleaseRepository;
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

        // dosageFormCd는 자유형식이 아니라 admin 공통코드 DOSAGE_FORM_CD의 3개 값("01"/"02"/"03")만 허용한다.
        // 수납(billing) 쪽이 이 3개 대표 항목으로만 수가를 계산하기로 확정했기 때문에(21.10절),
        // 여기서 미리 걸러내지 않으면 잘못된 코드가 그대로 저장돼 나중에 수납 연동에서 조용히 깨진다.
        for (PrescriptionItemEvent itemEvent : event.items()) {
            if (!DosageFormCode.isValid(itemEvent.dosageFormCd())) {
                log.error("허용되지 않는 dosageFormCd. prescriptionId={}, ediCode={}, dosageFormCd={} (허용값: 01/02/03)",
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

    /**
     * 조제완료 처리. 완료 즉시 결과 이벤트를 발행해서 처방코어/응급/병동에 동시 통지한다.
     * 이미 처리된(DISPENSED/REJECTED) 건은 다시 처리할 수 없다(PHM010).
     */
    @Transactional
    public void dispense(String prescriptionLinkId) {
        PrescriptionLink link = getReceivedLinkOrThrow(prescriptionLinkId);

        Dispensing dispensing = dispensingRepository.save(new Dispensing(link, LocalDate.now()));

        List<PrescriptionItemLink> items = prescriptionItemLinkRepository
                .findByPrescriptionLink_PrescriptionLinkId(prescriptionLinkId);
        for (PrescriptionItemLink item : items) {
            // 조제완료 = 처방 항목만큼 약국 재고에서 실제로 빠져나가는 시점. 재고가 모자라면 조제 자체를 완료할 수 없다(PHM008).
            StockMovementService.Result result = stockMovementService.decreaseFefo(
                    item.getMedicationId(), item.getDosageQty(), StockMovementService.STOCK_TX_TYPE_DISPENSING,
                    dispensing.getDispensingId(), SOURCE_FORM_TYPE_DISPENSING, DISPENSED_BY_PLACEHOLDER);
            dispensingItemRepository.save(new DispensingItem(
                    dispensing, item, result.lot(), item.getDosageQty(), item.getDosageQty()));
        }

        link.dispense();
        prescriptionResultPublisher.publish(link);
    }

    /**
     * 조제취소. 조제완료 때 실제로 빠져나간 재고를 그 당시 사용한 로트 그대로 복구하고,
     * 처방전 상태를 RECEIVED로 되돌려 다시 조제완료/거절을 받을 수 있게 한다.
     * 결과 이벤트는 재발행하지 않는다 - 조제완료 통지를 받은 쪽(처방코어/응급/병동)에 취소까지
     * 알리는 것은 이번 범위 밖이라, 필요해지면 별도 이벤트 타입으로 설계해야 한다.
     */
    @Transactional
    public void cancelDispense(String prescriptionLinkId, String reason) {
        if (reason.getBytes(StandardCharsets.UTF_8).length > CANCEL_REASON_MAX_BYTES) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        PrescriptionLink link = prescriptionLinkRepository.findById(prescriptionLinkId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRESCRIPTION_NOT_FOUND));

        Dispensing dispensing = dispensingRepository
                .findByPrescriptionLink_PrescriptionLinkIdAndDispenseStatusCd(prescriptionLinkId, Dispensing.STATUS_DISPENSED)
                .orElseThrow(() -> new BusinessException(ErrorCode.DISPENSING_NOT_FOUND));

        List<DispensingItem> items = dispensingItemRepository.findByDispensing_DispensingId(dispensing.getDispensingId());
        for (DispensingItem item : items) {
            stockMovementService.increaseLot(
                    item.getMedicationLot(), item.getDispensedQty(), StockMovementService.STOCK_TX_TYPE_DISPENSING_CANCEL,
                    dispensing.getDispensingId(), SOURCE_FORM_TYPE_DISPENSING_CANCEL, DISPENSED_BY_PLACEHOLDER);
        }

        dispensingCancelRepository.save(new DispensingCancel(dispensing, LocalDate.now(), reason, DISPENSED_BY_PLACEHOLDER));
        dispensing.cancel();
        link.backToReceived();
    }

    /**
     * 조제거절 처리. reason은 응급/병동 화면에 그대로 노출될 수 있어 내부 사유가 아니라
     * 사용자에게 보여줄 수 있는 문구로 받는다.
     */
    @Transactional
    public void reject(String prescriptionLinkId, String reason) {
        PrescriptionLink link = getReceivedLinkOrThrow(prescriptionLinkId);
        link.reject(reason);
        prescriptionResultPublisher.publish(link);
    }

    private PrescriptionLink getReceivedLinkOrThrow(String prescriptionLinkId) {
        PrescriptionLink link = prescriptionLinkRepository.findById(prescriptionLinkId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRESCRIPTION_NOT_FOUND));

        if (link.getStatus() != PrescriptionStatus.RECEIVED) {
            log.warn("이미 처리된 처방전에 대한 재처리 시도. prescriptionLinkId={}, status={}",
                    prescriptionLinkId, link.getStatus());
            throw new BusinessException(ErrorCode.PRESCRIPTION_ALREADY_PROCESSED);
        }
        return link;
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

        // 조제완료(DISPENSED) 상태일 때만 활성 Dispensing이 있다. 조제취소돼 RECEIVED로 돌아갔으면
        // 이전 Dispensing은 CANCELLED라 여기 안 잡히고, 항목별 dispensingItemId/불출 정보도 비게 된다
        // (반납/불출은 전부 "지금 조제완료 상태인 건"에만 의미가 있으므로 이게 맞다).
        Dispensing activeDispensing = dispensingRepository
                .findByPrescriptionLink_PrescriptionLinkIdAndDispenseStatusCd(prescriptionLinkId, Dispensing.STATUS_DISPENSED)
                .orElse(null);

        Map<String, DispensingItem> dispensingItemByPrescriptionItemLinkId = new HashMap<>();
        ReleaseInfoResponse release = null;
        if (activeDispensing != null) {
            for (DispensingItem dispensingItem : dispensingItemRepository.findByDispensing_DispensingId(activeDispensing.getDispensingId())) {
                dispensingItemByPrescriptionItemLinkId.put(
                        dispensingItem.getPrescriptionItemLink().getPrescriptionItemLinkId(), dispensingItem);
            }
            // RELEASED든 CANCELLED든 상태 무관하게 보여준다 — 취소된 적이 있으면 이 Dispensing으로는
            // 다시 불출할 수 없다는 걸(DB 유니크 제약) 화면에서도 알 수 있어야 "불출" 버튼을 다시
            // 눌렀다가 에러를 보는 일이 없다.
            MedicationRelease existingRelease = medicationReleaseRepository
                    .findByDispensing_DispensingId(activeDispensing.getDispensingId())
                    .orElse(null);
            if (existingRelease != null) {
                release = ReleaseInfoResponse.from(existingRelease);
            }
        }

        List<PrescriptionItemResponse> items = prescriptionItemLinkRepository
                .findByPrescriptionLink_PrescriptionLinkId(prescriptionLinkId)
                .stream()
                .map(item -> PrescriptionItemResponse.from(
                        item, dispensingItemByPrescriptionItemLinkId.get(item.getPrescriptionItemLinkId())))
                .toList();

        return PrescriptionDetailResponse.from(link, items, release);
    }
}
