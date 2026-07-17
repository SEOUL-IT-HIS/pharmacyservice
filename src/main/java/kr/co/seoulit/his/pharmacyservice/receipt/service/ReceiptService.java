package kr.co.seoulit.his.pharmacyservice.receipt.service;

import kr.co.seoulit.his.pharmacyservice.common.exception.BusinessException;
import kr.co.seoulit.his.pharmacyservice.common.exception.ErrorCode;
import kr.co.seoulit.his.pharmacyservice.inventory.entity.MedicationLot;
import kr.co.seoulit.his.pharmacyservice.inventory.entity.MedicationStock;
import kr.co.seoulit.his.pharmacyservice.inventory.repository.MedicationLotRepository;
import kr.co.seoulit.his.pharmacyservice.inventory.repository.MedicationStockRepository;
import kr.co.seoulit.his.pharmacyservice.receipt.dto.ReceiptCreateRequest;
import kr.co.seoulit.his.pharmacyservice.receipt.dto.ReceiptCreateResponse;
import kr.co.seoulit.his.pharmacyservice.receipt.dto.ReceiptItemRequest;
import kr.co.seoulit.his.pharmacyservice.receipt.dto.ReceiptItemResult;
import kr.co.seoulit.his.pharmacyservice.receipt.entity.InventoryMovement;
import kr.co.seoulit.his.pharmacyservice.receipt.entity.MedicationReceipt;
import kr.co.seoulit.his.pharmacyservice.receipt.entity.MedicationReceiptItem;
import kr.co.seoulit.his.pharmacyservice.receipt.repository.InventoryMovementRepository;
import kr.co.seoulit.his.pharmacyservice.receipt.repository.MedicationReceiptItemRepository;
import kr.co.seoulit.his.pharmacyservice.receipt.repository.MedicationReceiptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ReceiptService {

    private static final String STOCK_TX_TYPE_RECEIPT = "01";
    private static final String SOURCE_FORM_TYPE_RECEIPT = "RECEIPT";

    private final MedicationReceiptRepository medicationReceiptRepository;
    private final MedicationReceiptItemRepository medicationReceiptItemRepository;
    private final MedicationLotRepository medicationLotRepository;
    private final MedicationStockRepository medicationStockRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    public ReceiptService(MedicationReceiptRepository medicationReceiptRepository,
                           MedicationReceiptItemRepository medicationReceiptItemRepository,
                           MedicationLotRepository medicationLotRepository,
                           MedicationStockRepository medicationStockRepository,
                           InventoryMovementRepository inventoryMovementRepository) {
        this.medicationReceiptRepository = medicationReceiptRepository;
        this.medicationReceiptItemRepository = medicationReceiptItemRepository;
        this.medicationLotRepository = medicationLotRepository;
        this.medicationStockRepository = medicationStockRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
    }

    @Transactional
    public ReceiptCreateResponse createReceipt(ReceiptCreateRequest request) {
        validateNoDuplicateItems(request.items());

        MedicationReceipt receipt = medicationReceiptRepository.save(
                new MedicationReceipt(request.supplierId(), request.storageLocationId(),
                        request.receiptDt(), request.receivedById()));

        LocalDateTime movementAt = LocalDateTime.now();
        List<ReceiptItemResult> results = new ArrayList<>();

        for (ReceiptItemRequest itemRequest : request.items()) {
            results.add(receiveItem(receipt, itemRequest, request.storageLocationId(), request.receivedById(), movementAt));
        }

        return new ReceiptCreateResponse(receipt.getMedicationReceiptId(), results);
    }

    private ReceiptItemResult receiveItem(MedicationReceipt receipt, ReceiptItemRequest itemRequest,
                                           String storageLocationId, String receivedById, LocalDateTime movementAt) {
        MedicationLot lot = medicationLotRepository
                .findByMedicationIdAndLotNo(itemRequest.medicationId(), itemRequest.lotNo())
                .orElseGet(() -> medicationLotRepository.save(new MedicationLot(
                        itemRequest.medicationId(), itemRequest.lotNo(), itemRequest.expirationDt(),
                        itemRequest.manufactureDt(), itemRequest.unitCd())));

        MedicationReceiptItem receiptItem = medicationReceiptItemRepository.save(
                new MedicationReceiptItem(receipt, lot, itemRequest.receiptQty(), itemRequest.unitPrice()));

        MedicationStock stock = medicationStockRepository
                .findByMedicationLot_MedicationLotIdAndStorageLocationId(lot.getMedicationLotId(), storageLocationId)
                .orElseGet(() -> medicationStockRepository.save(new MedicationStock(lot, storageLocationId)));

        BigDecimal beforeQty = stock.increaseQty(itemRequest.receiptQty(), movementAt);
        BigDecimal afterQty = stock.getCurrentQty();

        inventoryMovementRepository.save(new InventoryMovement(
                stock, STOCK_TX_TYPE_RECEIPT, itemRequest.receiptQty(), beforeQty, afterQty,
                receipt.getMedicationReceiptId(), SOURCE_FORM_TYPE_RECEIPT, movementAt, receivedById));

        return new ReceiptItemResult(receiptItem.getMedicationReceiptItemId(), lot.getMedicationLotId(),
                stock.getMedicationStockId(), stock.getCurrentQty());
    }

    private void validateNoDuplicateItems(List<ReceiptItemRequest> items) {
        Set<String> seen = new HashSet<>();
        for (ReceiptItemRequest item : items) {
            String key = item.medicationId() + "|" + item.lotNo();
            if (!seen.add(key)) {
                throw new BusinessException(ErrorCode.DUPLICATE_RECEIPT_ITEM);
            }
        }
    }
}
