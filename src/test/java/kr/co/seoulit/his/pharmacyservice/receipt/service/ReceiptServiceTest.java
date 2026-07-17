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
import kr.co.seoulit.his.pharmacyservice.receipt.entity.InventoryMovement;
import kr.co.seoulit.his.pharmacyservice.receipt.entity.MedicationReceipt;
import kr.co.seoulit.his.pharmacyservice.receipt.entity.MedicationReceiptItem;
import kr.co.seoulit.his.pharmacyservice.receipt.repository.InventoryMovementRepository;
import kr.co.seoulit.his.pharmacyservice.receipt.repository.MedicationReceiptItemRepository;
import kr.co.seoulit.his.pharmacyservice.receipt.repository.MedicationReceiptRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceTest {

    @Mock
    private MedicationReceiptRepository medicationReceiptRepository;
    @Mock
    private MedicationReceiptItemRepository medicationReceiptItemRepository;
    @Mock
    private MedicationLotRepository medicationLotRepository;
    @Mock
    private MedicationStockRepository medicationStockRepository;
    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @InjectMocks
    private ReceiptService receiptService;

    private ReceiptItemRequest itemRequest(BigDecimal qty) {
        return new ReceiptItemRequest("MEDICATION-001", "LOT-2026-001",
                LocalDate.of(2027, 12, 31), LocalDate.of(2026, 6, 1), "EA", qty, BigDecimal.valueOf(500));
    }

    @Test
    void createReceipt_createsNewLotAndNewStock_whenNoneExist() {
        ReceiptCreateRequest request = new ReceiptCreateRequest(
                "SUPPLIER-001", "LOCATION-001", LocalDate.of(2026, 7, 16), "STAFF-001",
                List.of(itemRequest(BigDecimal.TEN)));

        when(medicationReceiptRepository.save(any(MedicationReceipt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(medicationLotRepository.findByMedicationIdAndLotNo("MEDICATION-001", "LOT-2026-001"))
                .thenReturn(Optional.empty());
        when(medicationLotRepository.save(any(MedicationLot.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(medicationReceiptItemRepository.save(any(MedicationReceiptItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(medicationStockRepository.findByMedicationLot_MedicationLotIdAndStorageLocationId(any(), any()))
                .thenReturn(Optional.empty());
        when(medicationStockRepository.save(any(MedicationStock.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(inventoryMovementRepository.save(any(InventoryMovement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReceiptCreateResponse response = receiptService.createReceipt(request);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).currentQty()).isEqualByComparingTo(BigDecimal.TEN);
        verify(medicationLotRepository).save(any(MedicationLot.class));
        verify(medicationStockRepository).save(any(MedicationStock.class));

        ArgumentCaptor<InventoryMovement> movementCaptor = ArgumentCaptor.forClass(InventoryMovement.class);
        verify(inventoryMovementRepository).save(movementCaptor.capture());
        InventoryMovement movement = movementCaptor.getValue();
        assertThat(movement.getStockTxTypeCd()).isEqualTo("01");
        assertThat(movement.getBeforeQty()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(movement.getAfterQty()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(movement.getSourceFormTypeCd()).isEqualTo("RECEIPT");
        assertThat(movement.getMovedById()).isEqualTo("STAFF-001");
    }

    @Test
    void createReceipt_reusesExistingLotAndIncreasesExistingStock() {
        ReceiptCreateRequest request = new ReceiptCreateRequest(
                "SUPPLIER-001", "LOCATION-001", LocalDate.of(2026, 7, 16), "STAFF-001",
                List.of(itemRequest(BigDecimal.valueOf(5))));

        MedicationLot existingLot = new MedicationLot("MEDICATION-001", "LOT-2026-001",
                LocalDate.of(2027, 12, 31), LocalDate.of(2026, 6, 1), "EA");
        MedicationStock existingStock = new MedicationStock(existingLot, "LOCATION-001");
        existingStock.increaseQty(BigDecimal.valueOf(20), null);

        when(medicationReceiptRepository.save(any(MedicationReceipt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(medicationLotRepository.findByMedicationIdAndLotNo("MEDICATION-001", "LOT-2026-001"))
                .thenReturn(Optional.of(existingLot));
        when(medicationReceiptItemRepository.save(any(MedicationReceiptItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(medicationStockRepository.findByMedicationLot_MedicationLotIdAndStorageLocationId(
                existingLot.getMedicationLotId(), "LOCATION-001"))
                .thenReturn(Optional.of(existingStock));
        when(inventoryMovementRepository.save(any(InventoryMovement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReceiptCreateResponse response = receiptService.createReceipt(request);

        assertThat(response.items().get(0).currentQty()).isEqualByComparingTo(BigDecimal.valueOf(25));
        verify(medicationLotRepository, never()).save(any(MedicationLot.class));
        verify(medicationStockRepository, never()).save(any(MedicationStock.class));
    }

    @Test
    void createReceipt_throwsDuplicateError_whenSameMedicationAndLotRepeated() {
        ReceiptCreateRequest request = new ReceiptCreateRequest(
                "SUPPLIER-001", "LOCATION-001", LocalDate.of(2026, 7, 16), "STAFF-001",
                List.of(itemRequest(BigDecimal.TEN), itemRequest(BigDecimal.ONE)));

        assertThatThrownBy(() -> receiptService.createReceipt(request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_RECEIPT_ITEM);

        verify(medicationReceiptRepository, never()).save(any());
    }
}
