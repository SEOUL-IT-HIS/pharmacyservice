package kr.co.seoulit.his.pharmacyservice.inventory.service;

import kr.co.seoulit.his.pharmacyservice.common.BusinessException;
import kr.co.seoulit.his.pharmacyservice.common.ErrorCode;
import kr.co.seoulit.his.pharmacyservice.inventory.entity.MedicationLot;
import kr.co.seoulit.his.pharmacyservice.inventory.entity.MedicationStock;
import kr.co.seoulit.his.pharmacyservice.inventory.repository.MedicationStockRepository;
import kr.co.seoulit.his.pharmacyservice.receipt.entity.InventoryMovement;
import kr.co.seoulit.his.pharmacyservice.receipt.repository.InventoryMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 출고/폐기/조제/조제취소/반납이 공통으로 쓰는 재고 차감·복구 + 재고 이력(INVENTORY_MOVEMENT) 기록.
 *
 * INVENTORY_MOVEMENT에는 DB CHECK 제약(CK_INVENTORY_MOVEMENT_3: AFTER_QTY = BEFORE_QTY + MOVEMENT_QTY)이 있어서,
 * 차감할 때는 MOVEMENT_QTY를 음수로 기록해야 한다. (예전 출고 코드는 양수로 넣어서 이 제약에 걸렸다.)
 */
@Service
public class StockMovementService {

    // 재고변동유형코드 — ADM 공통코드 PHM_STOCK_TX_TYPE 값과 맞춘다(01~03은 등록돼 있음, 04~06은 등록 필요).
    public static final String STOCK_TX_TYPE_RECEIPT = "01";
    public static final String STOCK_TX_TYPE_ISSUANCE = "02";
    public static final String STOCK_TX_TYPE_DISPOSAL = "03";
    public static final String STOCK_TX_TYPE_DISPENSING = "04";
    public static final String STOCK_TX_TYPE_DISPENSING_CANCEL = "05";
    public static final String STOCK_TX_TYPE_RETURN = "06";

    private final MedicationStockRepository medicationStockRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    public StockMovementService(MedicationStockRepository medicationStockRepository,
                                 InventoryMovementRepository inventoryMovementRepository) {
        this.medicationStockRepository = medicationStockRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
    }

    /**
     * 유효기간이 빠른 로트(FEFO)부터 수량이 충분한 재고 하나를 골라 차감한다.
     * 여러 로트로 쪼개서 차감하는 것은 지원하지 않는다(학습용 단순화). 충분한 재고가 없으면 PHM008.
     * 호출 쪽(출고/폐기 등)이 어느 로트에서 차감됐는지 알아야 상세 테이블에 기록할 수 있어 로트를 같이 반환한다.
     */
    @Transactional
    public Result decreaseFefo(String medicationId, BigDecimal qty, String stockTxTypeCd,
                                String sourceFormId, String sourceFormTypeCd, String movedById) {
        MedicationStock stock = medicationStockRepository
                .findAvailableByMedicationIdOrderByExpirationDtAsc(medicationId)
                .stream()
                .filter(s -> s.getCurrentQty().compareTo(qty) >= 0)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INSUFFICIENT_STOCK));

        return decrease(stock, qty, stockTxTypeCd, sourceFormId, sourceFormTypeCd, movedById);
    }

    /** 지정한 로트의 재고에서 차감한다 (반납약품폐기처럼 로트가 이미 정해진 경우). */
    @Transactional
    public Result decreaseLot(MedicationLot lot, BigDecimal qty, String stockTxTypeCd,
                               String sourceFormId, String sourceFormTypeCd, String movedById) {
        MedicationStock stock = getStockOfLot(lot);
        if (stock.getCurrentQty().compareTo(qty) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
        }
        return decrease(stock, qty, stockTxTypeCd, sourceFormId, sourceFormTypeCd, movedById);
    }

    /** 지정한 로트의 재고를 늘린다 (조제취소/반납처럼 원래 로트로 되돌리는 경우). */
    @Transactional
    public Result increaseLot(MedicationLot lot, BigDecimal qty, String stockTxTypeCd,
                               String sourceFormId, String sourceFormTypeCd, String movedById) {
        MedicationStock stock = getStockOfLot(lot);

        LocalDateTime movementAt = LocalDateTime.now();
        BigDecimal beforeQty = stock.increaseQty(qty, movementAt);
        BigDecimal afterQty = stock.getCurrentQty();

        InventoryMovement movement = inventoryMovementRepository.save(new InventoryMovement(
                stock, stockTxTypeCd, qty, beforeQty, afterQty,
                sourceFormId, sourceFormTypeCd, movementAt, movedById));
        return new Result(stock, movement);
    }

    private Result decrease(MedicationStock stock, BigDecimal qty, String stockTxTypeCd,
                             String sourceFormId, String sourceFormTypeCd, String movedById) {
        LocalDateTime movementAt = LocalDateTime.now();
        BigDecimal beforeQty = stock.decreaseQty(qty, movementAt);
        BigDecimal afterQty = stock.getCurrentQty();

        // 차감은 음수로 기록 (AFTER_QTY = BEFORE_QTY + MOVEMENT_QTY 제약)
        InventoryMovement movement = inventoryMovementRepository.save(new InventoryMovement(
                stock, stockTxTypeCd, qty.negate(), beforeQty, afterQty,
                sourceFormId, sourceFormTypeCd, movementAt, movedById));
        return new Result(stock, movement);
    }

    /** 차감/증가 처리 결과 — 어느 재고(= 어느 로트)에서 처리됐는지와 그 재고 이력 */
    public record Result(MedicationStock stock, InventoryMovement movement) {
        public MedicationLot lot() {
            return stock.getMedicationLot();
        }
    }

    // DISPENSING_ITEM 등은 로트만 기억하고 보관위치는 모르므로, 그 로트의 재고 중 첫 번째 것을 쓴다.
    private MedicationStock getStockOfLot(MedicationLot lot) {
        return medicationStockRepository
                .findFirstByMedicationLot_MedicationLotIdOrderByStorageLocationIdAsc(lot.getMedicationLotId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICATION_STOCK_NOT_FOUND));
    }
}
