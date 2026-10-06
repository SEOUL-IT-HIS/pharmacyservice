package kr.co.seoulit.his.pharmacyservice.inventory.service;

import kr.co.seoulit.his.pharmacyservice.common.BusinessException;
import kr.co.seoulit.his.pharmacyservice.common.ErrorCode;
import kr.co.seoulit.his.pharmacyservice.inventory.dto.InventoryMovementResponse;
import kr.co.seoulit.his.pharmacyservice.inventory.dto.InventoryResponse;
import kr.co.seoulit.his.pharmacyservice.inventory.entity.MedicationStock;
import kr.co.seoulit.his.pharmacyservice.inventory.repository.MedicationStockRepository;
import kr.co.seoulit.his.pharmacyservice.medication.entity.Medication;
import kr.co.seoulit.his.pharmacyservice.medication.repository.MedicationRepository;
import kr.co.seoulit.his.pharmacyservice.receipt.repository.InventoryMovementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class InventoryService {

    private final MedicationStockRepository medicationStockRepository;
    private final MedicationRepository medicationRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    public InventoryService(MedicationStockRepository medicationStockRepository,
                             MedicationRepository medicationRepository,
                             InventoryMovementRepository inventoryMovementRepository) {
        this.medicationStockRepository = medicationStockRepository;
        this.medicationRepository = medicationRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
    }

    /** 약품 상세(품목 중심 워크스페이스)의 "최근 입출고 내역" 패널용 — 품목 하나의 이동 이력을 최신순 상위 limit건만 */
    public List<InventoryMovementResponse> findRecentMovements(String medicationId, int limit) {
        Pageable pageable = PageRequest.of(0, Math.max(1, limit));
        return inventoryMovementRepository.findByMedicationIdOrderByMovementAtDesc(medicationId, pageable)
                .stream()
                .map(InventoryMovementResponse::from)
                .toList();
    }

    /**
     * 재고부족 목록 — 현재 수량이 threshold 이하인 재고를 수량이 적은 순으로 반환한다.
     * (예전엔 PL/SQL 프로시저 PROC_PHARMACY_2를 MyBatis로 호출했지만 JPA 쿼리로 바꿨다.)
     */
    public List<InventoryResponse> findLowStock(int threshold) {
        // 음수 기준값은 의미가 없으므로 400으로 막는다.
        if (threshold < 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        List<MedicationStock> stocks = medicationStockRepository.findLowStock(BigDecimal.valueOf(threshold));
        Map<Long, String> nameById = loadMedicationNames(stocks);
        return stocks.stream()
                .map(stock -> InventoryResponse.from(
                        stock, nameById.get(parseMedicationId(stock.getMedicationLot().getMedicationId()))))
                .toList();
    }

    public Page<InventoryResponse> search(String medicationId, String lotNo, String storageLocationId,
                                           LocalDate expirationFrom, LocalDate expirationTo, Pageable pageable) {
        Page<MedicationStock> stocks = medicationStockRepository
                .search(medicationId, lotNo, storageLocationId, expirationFrom, expirationTo, pageable);

        Map<Long, String> nameById = loadMedicationNames(stocks.getContent());

        return stocks.map(stock -> InventoryResponse.from(
                stock, nameById.get(parseMedicationId(stock.getMedicationLot().getMedicationId()))));
    }

    public InventoryResponse getDetail(String medicationStockId) {
        MedicationStock stock = medicationStockRepository.findByIdWithLot(medicationStockId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICATION_STOCK_NOT_FOUND));

        Long medicationId = parseMedicationId(stock.getMedicationLot().getMedicationId());
        String medicationName = medicationId == null ? null
                : medicationRepository.findById(medicationId).map(Medication::getMedicationName).orElse(null);

        return InventoryResponse.from(stock, medicationName);
    }

    /**
     * MEDICATION_LOT.MEDICATION_ID는 문자열로 저장돼 있어서(약품 마스터의 Long PK를
     * 문자열화한 값), 재고 화면에 약품명을 같이 보여주기 위해 여기서 한 번에 조회해온다.
     */
    private Map<Long, String> loadMedicationNames(List<MedicationStock> stocks) {
        Set<Long> ids = stocks.stream()
                .map(s -> parseMedicationId(s.getMedicationLot().getMedicationId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (ids.isEmpty()) {
            return Map.of();
        }

        // Collectors.toMap은 값이 null이면 예외를 던지므로(약품명이 비어있는 데이터가 있을 수 있어)
        // HashMap에 직접 넣는 방식으로 처리한다.
        Map<Long, String> nameById = new HashMap<>();
        medicationRepository.findAllById(ids)
                .forEach(medication -> nameById.put(medication.getMedicationId(), medication.getMedicationName()));
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
