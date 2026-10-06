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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
     * 유효기간이 빠른 로트(FEFO)부터 차례로 차감한다. 한 로트의 재고가 모자라면 다음 로트에서 이어서
     * 차감해 여러 로트로 나뉠 수 있다. 전체 재고가 모자라면 아무것도 차감하지 않고 PHM008을 던진다.
     * 호출 쪽(출고/폐기/조제)이 어느 로트에서 얼마나 차감됐는지 알아야 상세 테이블에 기록할 수 있어
     * 로트별 결과를 목록으로 반환한다.
     */
    @Transactional
    public List<Result> decreaseFefo(String medicationId, BigDecimal qty, String stockTxTypeCd,
                                      String sourceFormId, String sourceFormTypeCd, String movedById) {
        List<MedicationStock> stocks = medicationStockRepository
                .findAvailableByMedicationIdOrderByExpirationDtAsc(medicationId);

        BigDecimal available = stocks.stream()
                .map(MedicationStock::getCurrentQty)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (available.compareTo(qty) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
        }

        List<Result> results = new ArrayList<>();
        BigDecimal remaining = qty;
        for (MedicationStock stock : stocks) {
            if (remaining.signum() <= 0) {
                break;
            }
            BigDecimal take = stock.getCurrentQty().min(remaining);
            results.add(decrease(stock, take, stockTxTypeCd, sourceFormId, sourceFormTypeCd, movedById));
            remaining = remaining.subtract(take);
        }
        return results;
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

        /** 이번 처리로 실제 움직인 수량(항상 양수) */
        public BigDecimal qty() {
            return movement.getMovementQty().abs();
        }
    }

    /** 로트 하나에서 처리된 총 수량 (같은 로트가 보관위치 두 곳에 있어 두 번 차감됐으면 합쳐서) */
    public record LotQty(MedicationLot lot, BigDecimal qty) {
    }

    /** 차감 결과를 로트별로 합친다 — 조제/출고/폐기 상세 테이블은 로트 단위로 한 줄씩 기록한다. */
    public static List<LotQty> groupByLot(List<Result> results) {
        Map<String, LotQty> byLotId = new LinkedHashMap<>();
        for (Result result : results) {
            MedicationLot lot = result.lot();
            byLotId.merge(lot.getMedicationLotId(), new LotQty(lot, result.qty()),
                    (a, b) -> new LotQty(a.lot(), a.qty().add(b.qty())));
        }
        return new ArrayList<>(byLotId.values());
    }

    // DISPENSING_ITEM 등은 로트만 기억하고 보관위치는 모르므로, 그 로트의 재고 중 첫 번째 것을 쓴다.
    private MedicationStock getStockOfLot(MedicationLot lot) {
        return medicationStockRepository
                .findFirstByMedicationLot_MedicationLotIdOrderByStorageLocationIdAsc(lot.getMedicationLotId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICATION_STOCK_NOT_FOUND));
    }
}
