package kr.co.seoulit.his.pharmacyservice.issuance.service;

import kr.co.seoulit.his.pharmacyservice.common.BusinessException;
import kr.co.seoulit.his.pharmacyservice.common.ErrorCode;
import kr.co.seoulit.his.pharmacyservice.inventory.entity.MedicationStock;
import kr.co.seoulit.his.pharmacyservice.inventory.repository.MedicationStockRepository;
import kr.co.seoulit.his.pharmacyservice.issuance.dto.IssuanceCreateRequest;
import kr.co.seoulit.his.pharmacyservice.issuance.dto.IssuanceListResponse;
import kr.co.seoulit.his.pharmacyservice.medication.repository.MedicationRepository;
import kr.co.seoulit.his.pharmacyservice.receipt.entity.InventoryMovement;
import kr.co.seoulit.his.pharmacyservice.receipt.repository.InventoryMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 약품 출고(HL2-8 등록 / HL2-9 조회).
 * 학습 목적 수준의 단순화: 출고 헤더 없이 InventoryMovement(STOCK_TX_TYPE_CD='02')를 출고 이력으로 사용하고,
 * 재고는 유효기간이 빠른 로트(FEFO)부터 하나만 골라 차감한다 (여러 로트로 쪼개서 출고하는 것은 지원하지 않음).
 */
@Service
public class IssuanceService {

    private static final String STOCK_TX_TYPE_ISSUANCE = "02";
    private static final String SOURCE_FORM_TYPE_ISSUANCE = "ISSUANCE";
    /** 출고 등록 화면에 담당자 입력란이 없어 임시로 고정값을 사용한다 (인증 연동 전까지의 학습용 단순화) */
    private static final String ISSUED_BY_PLACEHOLDER = "SYSTEM";

    private final MedicationStockRepository medicationStockRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final MedicationRepository medicationRepository;

    public IssuanceService(MedicationStockRepository medicationStockRepository,
                            InventoryMovementRepository inventoryMovementRepository,
                            MedicationRepository medicationRepository) {
        this.medicationStockRepository = medicationStockRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.medicationRepository = medicationRepository;
    }

    @Transactional
    public void create(IssuanceCreateRequest request) {
        List<MedicationStock> candidates =
                medicationStockRepository.findAvailableByMedicationIdOrderByExpirationDtAsc(request.medicationId());

        MedicationStock stock = candidates.stream()
                .filter(s -> s.getCurrentQty().compareTo(request.quantity()) >= 0)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INSUFFICIENT_STOCK));

        LocalDateTime movementAt = LocalDateTime.now();
        BigDecimal beforeQty = stock.decreaseQty(request.quantity(), movementAt);
        BigDecimal afterQty = stock.getCurrentQty();

        inventoryMovementRepository.save(new InventoryMovement(
                stock, STOCK_TX_TYPE_ISSUANCE, request.quantity(), beforeQty, afterQty,
                UUID.randomUUID().toString(), SOURCE_FORM_TYPE_ISSUANCE, movementAt, ISSUED_BY_PLACEHOLDER));
    }

    /** 출고 조회(HL2-9) 화면용 — 출고 이력 목록 */
    @Transactional(readOnly = true)
    public List<IssuanceListResponse> list() {
        List<InventoryMovement> movements =
                inventoryMovementRepository.findAllByStockTxTypeCdOrderByMovementAtDesc(STOCK_TX_TYPE_ISSUANCE);
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
