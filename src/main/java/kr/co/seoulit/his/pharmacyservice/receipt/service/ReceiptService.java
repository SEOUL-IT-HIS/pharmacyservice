package kr.co.seoulit.his.pharmacyservice.receipt.service;

import kr.co.seoulit.his.pharmacyservice.common.BusinessException;
import kr.co.seoulit.his.pharmacyservice.common.ErrorCode;
import kr.co.seoulit.his.pharmacyservice.inventory.entity.MedicationLot;
import kr.co.seoulit.his.pharmacyservice.inventory.entity.MedicationStock;
import kr.co.seoulit.his.pharmacyservice.inventory.repository.MedicationLotRepository;
import kr.co.seoulit.his.pharmacyservice.inventory.repository.MedicationStockRepository;
import kr.co.seoulit.his.pharmacyservice.medication.repository.MedicationRepository;
import kr.co.seoulit.his.pharmacyservice.receipt.dto.ReceiptCreateRequest;
import kr.co.seoulit.his.pharmacyservice.receipt.dto.ReceiptCreateResponse;
import kr.co.seoulit.his.pharmacyservice.receipt.dto.ReceiptItemRequest;
import kr.co.seoulit.his.pharmacyservice.receipt.dto.ReceiptItemResult;
import kr.co.seoulit.his.pharmacyservice.receipt.dto.ReceiptListResponse;
import kr.co.seoulit.his.pharmacyservice.receipt.entity.InventoryMovement;
import kr.co.seoulit.his.pharmacyservice.receipt.entity.MedicationReceipt;
import kr.co.seoulit.his.pharmacyservice.receipt.entity.MedicationReceiptItem;
import kr.co.seoulit.his.pharmacyservice.receipt.repository.InventoryMovementRepository;
import kr.co.seoulit.his.pharmacyservice.receipt.repository.MedicationReceiptItemRepository;
import kr.co.seoulit.his.pharmacyservice.receipt.repository.MedicationReceiptRepository;
import kr.co.seoulit.his.pharmacyservice.supplier.repository.SupplierRepository;
import kr.co.seoulit.his.pharmacyservice.storagelocation.repository.StorageLocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private final MedicationRepository medicationRepository;
    private final SupplierRepository supplierRepository;
    private final StorageLocationRepository storageLocationRepository;

    public ReceiptService(MedicationReceiptRepository medicationReceiptRepository,
                           MedicationReceiptItemRepository medicationReceiptItemRepository,
                           MedicationLotRepository medicationLotRepository,
                           MedicationStockRepository medicationStockRepository,
                           InventoryMovementRepository inventoryMovementRepository,
                           MedicationRepository medicationRepository,
                           SupplierRepository supplierRepository,
                           StorageLocationRepository storageLocationRepository) {
        this.medicationReceiptRepository = medicationReceiptRepository;
        this.medicationReceiptItemRepository = medicationReceiptItemRepository;
        this.medicationLotRepository = medicationLotRepository;
        this.medicationStockRepository = medicationStockRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.medicationRepository = medicationRepository;
        this.supplierRepository = supplierRepository;
        this.storageLocationRepository = storageLocationRepository;
    }

    @Transactional
    public ReceiptCreateResponse createReceipt(ReceiptCreateRequest request) {
        // 공급처/보관위치도 약품마스터(medicationId)와 같은 이유로 실제 등록된 값인지 먼저 막는다
        // — 화면이 Select로 바뀌어도 API를 직접 호출하면 임의 ID가 들어올 수 있다.
        if (!supplierRepository.existsById(request.supplierId())) {
            throw new BusinessException(ErrorCode.SUPPLIER_NOT_FOUND);
        }
        if (!storageLocationRepository.existsById(request.storageLocationId())) {
            throw new BusinessException(ErrorCode.STORAGE_LOCATION_NOT_FOUND);
        }
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

    /** 입고 조회(HL2-7) 화면용 — 약품별 입고 항목 목록 */
    @Transactional(readOnly = true)
    public List<ReceiptListResponse> list() {
        List<MedicationReceiptItem> items = medicationReceiptItemRepository.findAllWithLot();
        Map<Long, String> nameByMedicationId = loadMedicationNames(items);
        return items.stream()
                .map(item -> ReceiptListResponse.from(
                        item, nameByMedicationId.get(parseMedicationId(item.getMedicationLot().getMedicationId()))))
                .toList();
    }

    private Map<Long, String> loadMedicationNames(List<MedicationReceiptItem> items) {
        Set<Long> ids = new HashSet<>();
        for (MedicationReceiptItem item : items) {
            Long id = parseMedicationId(item.getMedicationLot().getMedicationId());
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

    private ReceiptItemResult receiveItem(MedicationReceipt receipt, ReceiptItemRequest itemRequest,
                                           String storageLocationId, String receivedById, LocalDateTime movementAt) {
        // 약품 마스터에 없는 medicationId로 입고하면 이름 없는 유령 재고 로트가 생기므로 먼저 막는다.
        Long medicationId = parseMedicationId(itemRequest.medicationId());
        if (medicationId == null || !medicationRepository.existsById(medicationId)) {
            throw new BusinessException(ErrorCode.MEDICATION_NOT_FOUND);
        }

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
