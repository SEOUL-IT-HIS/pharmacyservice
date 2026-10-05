package kr.co.seoulit.his.pharmacyservice.disposal.service;

import kr.co.seoulit.his.pharmacyservice.common.BusinessException;
import kr.co.seoulit.his.pharmacyservice.common.ErrorCode;
import kr.co.seoulit.his.pharmacyservice.disposal.dto.DisposalCreateRequest;
import kr.co.seoulit.his.pharmacyservice.inventory.entity.MedicationStock;
import kr.co.seoulit.his.pharmacyservice.inventory.repository.MedicationStockRepository;
import kr.co.seoulit.his.pharmacyservice.receipt.entity.InventoryMovement;
import kr.co.seoulit.his.pharmacyservice.receipt.repository.InventoryMovementRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 약품 폐기(HL2-10 등록).
 * 출고(IssuanceService)와 같은 단순화: 폐기 헤더 없이 InventoryMovement(STOCK_TX_TYPE_CD='03')를 폐기 이력으로 사용하고,
 * 재고는 유효기간이 빠른 로트(FEFO)부터 하나만 골라 차감한다 (여러 로트로 쪼개서 폐기하는 것은 지원하지 않음).
 * 폐기 사유는 INVENTORY_MOVEMENT에 담을 컬럼이 없어 로그로만 남긴다 (약품폐기 테이블은 ERD상 제안 단계, 미구현).
 */
@Slf4j
@Service
public class DisposalService {

    private static final String STOCK_TX_TYPE_DISPOSAL = "03";
    private static final String SOURCE_FORM_TYPE_DISPOSAL = "DISPOSAL";
    /** 폐기 등록 화면에 담당자 입력란이 없어 임시로 고정값을 사용한다 (출고와 동일한 학습용 단순화) */
    private static final String DISPOSED_BY_PLACEHOLDER = "SYSTEM";

    private final MedicationStockRepository medicationStockRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    public DisposalService(MedicationStockRepository medicationStockRepository,
                            InventoryMovementRepository inventoryMovementRepository) {
        this.medicationStockRepository = medicationStockRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
    }

    @Transactional
    public void create(DisposalCreateRequest request) {
        List<MedicationStock> candidates =
                medicationStockRepository.findAvailableByMedicationIdOrderByExpirationDtAsc(request.medicationId());

        MedicationStock stock = candidates.stream()
                .filter(s -> s.getCurrentQty().compareTo(request.quantity()) >= 0)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INSUFFICIENT_STOCK));

        LocalDateTime movementAt = LocalDateTime.now();
        BigDecimal beforeQty = stock.decreaseQty(request.quantity(), movementAt);
        BigDecimal afterQty = stock.getCurrentQty();

        String sourceFormId = UUID.randomUUID().toString();
        inventoryMovementRepository.save(new InventoryMovement(
                stock, STOCK_TX_TYPE_DISPOSAL, request.quantity(), beforeQty, afterQty,
                sourceFormId, SOURCE_FORM_TYPE_DISPOSAL, movementAt, DISPOSED_BY_PLACEHOLDER));

        log.info("약품 폐기 처리: sourceFormId={}, medicationId={}, quantity={}, reason={}",
                sourceFormId, request.medicationId(), request.quantity(), request.reason());
    }
}
