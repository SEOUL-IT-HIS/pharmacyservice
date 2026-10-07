package kr.co.seoulit.his.pharmacyservice.prescription.service;

import kr.co.seoulit.his.pharmacyservice.common.BusinessException;
import kr.co.seoulit.his.pharmacyservice.common.ErrorCode;
import kr.co.seoulit.his.pharmacyservice.inventory.repository.MedicationStockRepository;
import kr.co.seoulit.his.pharmacyservice.inventory.service.StockMovementService;
import kr.co.seoulit.his.pharmacyservice.medication.entity.Medication;
import kr.co.seoulit.his.pharmacyservice.medication.repository.MedicationRepository;
import kr.co.seoulit.his.pharmacyservice.medicationreturn.entity.MedicationReturnItem;
import kr.co.seoulit.his.pharmacyservice.medicationreturn.repository.MedicationReturnItemRepository;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.DispensingLotResponse;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.DosageFormCode;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionCancelledEvent;
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

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class PrescriptionService {

    private static final Logger log = LoggerFactory.getLogger(PrescriptionService.class);

    private static final String SOURCE_FORM_TYPE_DISPENSING = "DISPENSING";
    private static final String SOURCE_FORM_TYPE_DISPENSING_CANCEL = "DISPENSING_CANCEL";
    /** CANCEL_REASON_CD 컬럼(200바이트)에 들어가는 사유만 저장하고 넘으면 400으로 막는다 */
    private static final int CANCEL_REASON_MAX_BYTES = 200;
    private static final String SYSTEM_ACTOR = "SYSTEM-OPD";
    private static final String CANCEL_REASON_PREFIX = "[처방코어 취소] ";

    private final PrescriptionLinkRepository prescriptionLinkRepository;
    private final PrescriptionItemLinkRepository prescriptionItemLinkRepository;
    private final MedicationRepository medicationRepository;
    private final PrescriptionResultPublisher prescriptionResultPublisher;
    private final DispensingRepository dispensingRepository;
    private final DispensingItemRepository dispensingItemRepository;
    private final DispensingCancelRepository dispensingCancelRepository;
    private final StockMovementService stockMovementService;
    private final MedicationReleaseRepository medicationReleaseRepository;
    private final MedicationStockRepository medicationStockRepository;
    private final MedicationReturnItemRepository medicationReturnItemRepository;

    public PrescriptionService(PrescriptionLinkRepository prescriptionLinkRepository,
                                PrescriptionItemLinkRepository prescriptionItemLinkRepository,
                                MedicationRepository medicationRepository,
                                PrescriptionResultPublisher prescriptionResultPublisher,
                                DispensingRepository dispensingRepository,
                                DispensingItemRepository dispensingItemRepository,
                                DispensingCancelRepository dispensingCancelRepository,
                                StockMovementService stockMovementService,
                                MedicationReleaseRepository medicationReleaseRepository,
                                MedicationStockRepository medicationStockRepository,
                                MedicationReturnItemRepository medicationReturnItemRepository) {
        this.prescriptionLinkRepository = prescriptionLinkRepository;
        this.prescriptionItemLinkRepository = prescriptionItemLinkRepository;
        this.medicationRepository = medicationRepository;
        this.prescriptionResultPublisher = prescriptionResultPublisher;
        this.dispensingRepository = dispensingRepository;
        this.dispensingItemRepository = dispensingItemRepository;
        this.dispensingCancelRepository = dispensingCancelRepository;
        this.stockMovementService = stockMovementService;
        this.medicationReleaseRepository = medicationReleaseRepository;
        this.medicationStockRepository = medicationStockRepository;
        this.medicationReturnItemRepository = medicationReturnItemRepository;
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
        link.applyOrderMeta(event.encounterType(), event.priorityCode(), event.verbalYn());

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
     * 조제완료 처리. 완료 즉시 결과 이벤트를 발행해서 응급/병동에 동시 통지한다.
     * 이미 처리된(DISPENSED/REJECTED) 건은 다시 처리할 수 없다(PHM010).
     * 한 약품이 한 로트의 재고로 모자라면 유효기간이 빠른 로트부터 여러 로트에서 이어서 차감하고,
     * 로트마다 조제 상세(DispensingItem)를 한 줄씩 남긴다. 하나라도 전체 재고가 모자라면 전부 롤백된다.
     */
    @Transactional
    public void dispense(String prescriptionLinkId, String actorId) {
        PrescriptionLink link = getReceivedLinkOrThrow(prescriptionLinkId);

        Dispensing dispensing = dispensingRepository.save(new Dispensing(link, LocalDate.now(), actorId));

        List<PrescriptionItemLink> items = prescriptionItemLinkRepository
                .findByPrescriptionLink_PrescriptionLinkId(prescriptionLinkId);
        for (PrescriptionItemLink item : items) {
            DispenseQuantity.Calculation calc = DispenseQuantity.of(item);
            // 횟수/일수를 숫자로 못 읽어 1로 폴백된 경우는 그 구간만큼 덜 차감되고 있다는 뜻이라 추적할 수 있게 남긴다.
            if (calc.ambiguous()) {
                log.warn("frequency/durationDays를 숫자로 못 읽어서 그 구간은 1로 계산함(과소 차감 위험). "
                                + "prescriptionItemLinkId={}, frequency={}, durationDays={}",
                        item.getPrescriptionItemLinkId(), item.getFrequency(), item.getDurationDays());
            }

            List<StockMovementService.Result> results = stockMovementService.decreaseFefo(
                    item.getMedicationId(), calc.totalQty(), StockMovementService.STOCK_TX_TYPE_DISPENSING,
                    dispensing.getDispensingId(), SOURCE_FORM_TYPE_DISPENSING, actorId);
            for (StockMovementService.LotQty lotQty : StockMovementService.groupByLot(results)) {
                dispensingItemRepository.save(new DispensingItem(
                        dispensing, item, lotQty.lot(), calc.totalQty(), lotQty.qty()));
            }
        }

        link.dispense();
        prescriptionResultPublisher.publish(link);
    }

    /**
     * 조제취소. 조제완료 때 실제로 빠져나간 재고를 그 당시 사용한 로트 그대로 복구하고,
     * 처방전 상태를 RECEIVED로 되돌려 다시 조제완료/거절을 받을 수 있게 한다.
     * 결과 이벤트는 재발행하지 않는다 - 조제완료 통지를 받은 쪽(응급/병동)에 취소까지
     * 알리는 것은 이번 범위 밖이라, 필요해지면 별도 이벤트 타입으로 설계해야 한다.
     */
    @Transactional
    public void cancelDispense(String prescriptionLinkId, String reason, String actorId) {
        if (reason.getBytes(StandardCharsets.UTF_8).length > CANCEL_REASON_MAX_BYTES) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        PrescriptionLink link = prescriptionLinkRepository.findById(prescriptionLinkId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRESCRIPTION_NOT_FOUND));

        revertDispensing(link, reason, actorId);
        link.backToReceived();
    }

    /**
     * 처방코어의 취소 통보 처리. 처방코어로는 회신하지 않고 결과 이벤트도 발행하지 않는다.
     * - 접수(RECEIVED): 바로 CANCELLED.
     * - 조제완료(DISPENSED) & 불출 전: 재고를 로트 그대로 복구(조제취소)한 뒤 CANCELLED.
     * - 불출 이후: 되돌릴 수 없어 상태를 바꾸지 않고 로그만 남긴다(약사가 확인).
     * - 처방이 없거나 이미 CANCELLED/REJECTED면 무시한다(재전송 멱등).
     * 약사의 조제완료/거절과 겹치지 않도록 행을 잠그고 한 트랜잭션에서 처리한다.
     */
    @Transactional
    public void cancelByPrescription(PrescriptionCancelledEvent event) {
        PrescriptionLink link = prescriptionLinkRepository.findByPrescriptionIdForUpdate(event.prescriptionId())
                .orElse(null);
        if (link == null) {
            log.warn("취소 통보를 받았으나 약제에 없는 처방이라 무시함. prescriptionId={}", event.prescriptionId());
            return;
        }

        log.info("처방 취소 통보 수신. prescriptionId={}, 현재 상태={}, cancelledBy={}",
                event.prescriptionId(), link.getStatus(), event.cancelledBy());

        String cancelledBy = event.cancelledBy() == null || event.cancelledBy().isBlank()
                ? SYSTEM_ACTOR : event.cancelledBy();
        String reason = truncateBytes(event.cancelReason() == null ? "" : event.cancelReason(), CANCEL_REASON_MAX_BYTES);

        switch (link.getStatus()) {
            case CANCELLED -> log.warn("이미 취소된 처방의 취소 통보라 무시함. prescriptionId={}", event.prescriptionId());
            case REJECTED -> log.warn("이미 거절된 처방의 취소 통보라 무시함. prescriptionId={}", event.prescriptionId());
            case RECEIVED -> link.cancel(reason, cancelledBy);
            case DISPENSED -> {
                Dispensing dispensing = dispensingRepository
                        .findByPrescriptionLink_PrescriptionLinkIdAndDispenseStatusCd(
                                link.getPrescriptionLinkId(), Dispensing.STATUS_DISPENSED)
                        .orElseThrow(() -> new BusinessException(ErrorCode.DISPENSING_NOT_FOUND));
                if (isReleased(dispensing)) {
                    log.warn("불출이 끝난 처방이라 취소 통보를 반영하지 못함(수동 확인 필요). prescriptionId={}, cancelledBy={}, reason={}",
                            event.prescriptionId(), cancelledBy, reason);
                    // 상태는 그대로 두되 통보 사실을 남겨서 약사가 목록/상세 화면에서 확인할 수 있게 한다.
                    link.markCancelRefused(reason, cancelledBy);
                    return;
                }
                revertDispensing(link, truncateBytes(CANCEL_REASON_PREFIX + reason, CANCEL_REASON_MAX_BYTES), cancelledBy);
                link.cancel(reason, cancelledBy);
            }
        }
    }

    private boolean isReleased(Dispensing dispensing) {
        return medicationReleaseRepository.findByDispensing_DispensingIdAndReleaseStatusCd(
                dispensing.getDispensingId(), MedicationRelease.STATUS_RELEASED).isPresent();
    }

    /** 조제완료 때 차감된 재고를 로트 그대로 복구하고 조제 건을 취소 처리한다. 처방전 상태는 호출한 쪽이 정한다. */
    private void revertDispensing(PrescriptionLink link, String reason, String actorId) {
        Dispensing dispensing = dispensingRepository
                .findByPrescriptionLink_PrescriptionLinkIdAndDispenseStatusCd(
                        link.getPrescriptionLinkId(), Dispensing.STATUS_DISPENSED)
                .orElseThrow(() -> new BusinessException(ErrorCode.DISPENSING_NOT_FOUND));

        // 이미 환자/병동에 불출된 상태에서 조제를 취소하면, 나간 약의 재고가 되살아난다. 먼저 불출을 취소해야 한다.
        if (isReleased(dispensing)) {
            throw new BusinessException(ErrorCode.DISPENSE_CANCEL_BLOCKED_BY_RELEASE);
        }

        List<DispensingItem> items = dispensingItemRepository.findByDispensing_DispensingId(dispensing.getDispensingId());
        for (DispensingItem item : items) {
            stockMovementService.increaseLot(
                    item.getMedicationLot(), item.getDispensedQty(), StockMovementService.STOCK_TX_TYPE_DISPENSING_CANCEL,
                    dispensing.getDispensingId(), SOURCE_FORM_TYPE_DISPENSING_CANCEL, actorId);
        }

        dispensingCancelRepository.save(new DispensingCancel(dispensing, LocalDate.now(), reason, actorId));
        dispensing.cancel();
    }

    /** UTF-8 기준 maxBytes를 넘지 않게 문자 단위로 자른다(멀티바이트 문자가 중간에 잘리지 않도록). */
    private static String truncateBytes(String text, int maxBytes) {
        if (text.getBytes(StandardCharsets.UTF_8).length <= maxBytes) {
            return text;
        }
        StringBuilder sb = new StringBuilder();
        int bytes = 0;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            int len = new String(Character.toChars(cp)).getBytes(StandardCharsets.UTF_8).length;
            if (bytes + len > maxBytes) {
                break;
            }
            sb.appendCodePoint(cp);
            bytes += len;
            i += Character.charCount(cp);
        }
        return sb.toString();
    }

    /**
     * 조제거절 처리. reason은 응급/병동 화면에 그대로 노출될 수 있어 내부 사유가 아니라
     * 사용자에게 보여줄 수 있는 문구로 받는다.
     */
    @Transactional
    public void reject(String prescriptionLinkId, String reason, String actorId) {
        PrescriptionLink link = getReceivedLinkOrThrow(prescriptionLinkId);
        link.reject(reason, actorId);
        prescriptionResultPublisher.publish(link);
    }

    private PrescriptionLink getReceivedLinkOrThrow(String prescriptionLinkId) {
        PrescriptionLink link = prescriptionLinkRepository.findById(prescriptionLinkId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRESCRIPTION_NOT_FOUND));

        if (link.getStatus() == PrescriptionStatus.CANCELLED) {
            log.warn("취소된 처방전에 대한 처리 시도. prescriptionLinkId={}", prescriptionLinkId);
            throw new BusinessException(ErrorCode.PRESCRIPTION_CANCELLED);
        }
        if (link.getStatus() != PrescriptionStatus.RECEIVED) {
            log.warn("이미 처리된 처방전에 대한 재처리 시도. prescriptionLinkId={}, status={}",
                    prescriptionLinkId, link.getStatus());
            throw new BusinessException(ErrorCode.PRESCRIPTION_ALREADY_PROCESSED);
        }
        return link;
    }

    /**
     * 처방전 목록. stage로 업무 단계를 걸러 볼 수 있다 — RECEIVED(접수) / DISPENSED(조제완료, 불출 대기) /
     * RELEASED(불출완료) / RELEASE_CANCELLED(불출취소됨) / REJECTED(거절) / CANCELLED(처방코어 취소) / 없거나 ALL(전체).
     * 조제완료 건에는 불출 상태(releaseStatusCd)를 같이 내려준다.
     */
    public Page<PrescriptionListResponse> search(String prescriptionId, String patientId, String physicianId,
                                                  String departmentId, String stage, Pageable pageable) {
        Page<PrescriptionLink> page = prescriptionLinkRepository
                .search(prescriptionId, patientId, physicianId, departmentId, stage, pageable);

        Map<String, String> releaseStatusByLinkId = new HashMap<>();
        List<String> dispensedLinkIds = page.getContent().stream()
                .filter(link -> link.getStatus() == PrescriptionStatus.DISPENSED)
                .map(PrescriptionLink::getPrescriptionLinkId)
                .toList();
        if (!dispensedLinkIds.isEmpty()) {
            for (Object[] row : medicationReleaseRepository.findReleaseStatusesByPrescriptionLinkIds(dispensedLinkIds)) {
                releaseStatusByLinkId.put((String) row[0], (String) row[1]);
            }
        }

        return page.map(link -> PrescriptionListResponse.from(link, releaseStatusByLinkId.get(link.getPrescriptionLinkId())));
    }

    public PrescriptionDetailResponse getDetail(String prescriptionLinkId) {
        PrescriptionLink link = prescriptionLinkRepository.findById(prescriptionLinkId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRESCRIPTION_NOT_FOUND));

        // 조제완료(DISPENSED) 상태일 때만 활성 Dispensing이 있다. 조제취소돼 RECEIVED로 돌아갔으면
        // 이전 Dispensing은 CANCELLED라 여기 안 잡히고, 항목별 조제 로트/불출 정보도 비게 된다
        // (반납/불출은 전부 "지금 조제완료 상태인 건"에만 의미가 있으므로 이게 맞다).
        Dispensing activeDispensing = dispensingRepository
                .findByPrescriptionLink_PrescriptionLinkIdAndDispenseStatusCd(prescriptionLinkId, Dispensing.STATUS_DISPENSED)
                .orElse(null);

        // 처방항목 하나가 여러 로트에서 조제됐을 수 있어 항목ID별로 DispensingItem을 모은다.
        Map<String, List<DispensingItem>> dispensingItemsByPrescriptionItemLinkId = new HashMap<>();
        ReleaseInfoResponse release = null;
        if (activeDispensing != null) {
            for (DispensingItem dispensingItem : dispensingItemRepository.findByDispensing_DispensingId(activeDispensing.getDispensingId())) {
                dispensingItemsByPrescriptionItemLinkId
                        .computeIfAbsent(dispensingItem.getPrescriptionItemLink().getPrescriptionItemLinkId(), key -> new ArrayList<>())
                        .add(dispensingItem);
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
                .map(item -> toItemResponse(item,
                        dispensingItemsByPrescriptionItemLinkId.getOrDefault(item.getPrescriptionItemLinkId(), List.of())))
                .toList();

        return PrescriptionDetailResponse.from(link, items, release,
                activeDispensing == null ? null : activeDispensing.getDispensedById());
    }

    private PrescriptionItemResponse toItemResponse(PrescriptionItemLink item, List<DispensingItem> dispensingItems) {
        Medication medication = findMedication(item.getMedicationId());
        DispenseQuantity.Calculation calc = DispenseQuantity.of(item);
        BigDecimal availableQty = medicationStockRepository.sumCurrentQtyByMedicationId(item.getMedicationId());

        List<DispensingLotResponse> lots = dispensingItems.stream()
                .map(dispensingItem -> new DispensingLotResponse(
                        dispensingItem.getDispensingItemId(),
                        dispensingItem.getMedicationLot().getLotNo(),
                        dispensingItem.getMedicationLot().getExpirationDt(),
                        dispensingItem.getDispensedQty(),
                        medicationReturnItemRepository
                                .findByDispensingItem_DispensingItemId(dispensingItem.getDispensingItemId())
                                .stream()
                                .map(MedicationReturnItem::getReturnQty)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)))
                .toList();

        return PrescriptionItemResponse.from(
                item,
                medication == null ? null : medication.getMedicationName(),
                medication == null ? null : medication.getEdiCode(),
                calc.totalQty(), calc.ambiguous(), availableQty, lots);
    }

    private Medication findMedication(String medicationId) {
        try {
            return medicationRepository.findById(Long.parseLong(medicationId)).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
