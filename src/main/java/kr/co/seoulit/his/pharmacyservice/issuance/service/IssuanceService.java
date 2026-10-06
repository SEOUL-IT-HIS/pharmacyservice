package kr.co.seoulit.his.pharmacyservice.issuance.service;

import kr.co.seoulit.his.pharmacyservice.inventory.entity.MedicationLot;
import kr.co.seoulit.his.pharmacyservice.inventory.service.StockMovementService;
import kr.co.seoulit.his.pharmacyservice.issuance.dto.IssuanceCreateRequest;
import kr.co.seoulit.his.pharmacyservice.issuance.dto.IssuanceListResponse;
import kr.co.seoulit.his.pharmacyservice.issuance.entity.MedicationIssue;
import kr.co.seoulit.his.pharmacyservice.issuance.entity.MedicationIssueItem;
import kr.co.seoulit.his.pharmacyservice.issuance.repository.MedicationIssueItemRepository;
import kr.co.seoulit.his.pharmacyservice.issuance.repository.MedicationIssueRepository;
import kr.co.seoulit.his.pharmacyservice.medication.repository.MedicationRepository;
import kr.co.seoulit.his.pharmacyservice.receipt.entity.InventoryMovement;
import kr.co.seoulit.his.pharmacyservice.receipt.repository.InventoryMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 약품 출고(HL2-8 등록 / HL2-9 조회).
 * MEDICATION_ISSUE(헤더) + MEDICATION_ISSUE_ITEM(상세)에 정식으로 기록하고, 재고 차감은
 * 유효기간이 빠른 로트(FEFO)부터 차감하고, 한 로트로 모자라면 여러 로트로 나눠 차감한다.
 */
@Service
public class IssuanceService {

    /** 출고 유형 — ADM 공통코드에 아직 등록 전이라 임시로 "일반출고" 한 종류만 쓴다(학습용 단순화) */
    private static final String ISSUE_TYPE_GENERAL = "01";
    private static final String SOURCE_FORM_TYPE_ISSUANCE = "ISSUANCE";

    private final MedicationIssueRepository medicationIssueRepository;
    private final MedicationIssueItemRepository medicationIssueItemRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final MedicationRepository medicationRepository;
    private final StockMovementService stockMovementService;

    public IssuanceService(MedicationIssueRepository medicationIssueRepository,
                            MedicationIssueItemRepository medicationIssueItemRepository,
                            InventoryMovementRepository inventoryMovementRepository,
                            MedicationRepository medicationRepository,
                            StockMovementService stockMovementService) {
        this.medicationIssueRepository = medicationIssueRepository;
        this.medicationIssueItemRepository = medicationIssueItemRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.medicationRepository = medicationRepository;
        this.stockMovementService = stockMovementService;
    }

    // 마약류 입출고(controlleddrug 패키지)가 이 메서드를 그대로 재사용하면서, 방금 생긴
    // INVENTORY_MOVEMENT를 찾아 CONTROLLED_DRUG_RECORD를 남겨야 해서 헤더를 반환한다.
    @Transactional
    public MedicationIssue create(IssuanceCreateRequest request, String issuedById) {
        MedicationIssue issue = medicationIssueRepository.save(
                new MedicationIssue(issuedById, ISSUE_TYPE_GENERAL));

        // 유효기간이 빠른 로트부터 차감하고, 한 로트로 모자라면 다음 로트로 이어서 차감한다 — 로트마다 한 줄씩 기록.
        List<StockMovementService.Result> results = stockMovementService.decreaseFefo(
                request.medicationId(), request.quantity(), StockMovementService.STOCK_TX_TYPE_ISSUANCE,
                issue.getMedicationIssueId(), SOURCE_FORM_TYPE_ISSUANCE, issuedById);
        for (StockMovementService.LotQty lotQty : StockMovementService.groupByLot(results)) {
            medicationIssueItemRepository.save(new MedicationIssueItem(issue, lotQty.lot(), lotQty.qty()));
        }

        return issue;
    }

    /** 출고 조회(HL2-9) 화면용 — 출고 이력 목록 */
    @Transactional(readOnly = true)
    public List<IssuanceListResponse> list() {
        List<InventoryMovement> movements = inventoryMovementRepository
                .findAllByStockTxTypeCdOrderByMovementAtDesc(StockMovementService.STOCK_TX_TYPE_ISSUANCE);
        Map<Long, String> nameByMedicationId = loadMedicationNames(movements);
        return movements.stream()
                .map(m -> IssuanceListResponse.from(
                        m, nameByMedicationId.get(parseMedicationId(
                                m.getMedicationStock().getMedicationLot().getMedicationId()))))
                .toList();
    }

    private Map<Long, String> loadMedicationNames(List<InventoryMovement> movements) {
        Set<Long> ids = new HashSet<>();
        for (InventoryMovement movement : movements) {
            Long id = parseMedicationId(movement.getMedicationStock().getMedicationLot().getMedicationId());
            if (id != null) {
                ids.add(id);
            }
        }
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> nameById = new HashMap<>();
        medicationRepository.findAllById(ids)
                .forEach(m -> nameById.put(m.getMedicationId(), m.getMedicationName()));
        return nameById;
    }

    private Long parseMedicationId(String medicationId) {
        try {
            return Long.parseLong(medicationId);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
