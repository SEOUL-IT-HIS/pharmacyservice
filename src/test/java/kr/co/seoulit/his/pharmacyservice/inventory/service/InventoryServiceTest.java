package kr.co.seoulit.his.pharmacyservice.inventory.service;

import kr.co.seoulit.his.pharmacyservice.common.exception.BusinessException;
import kr.co.seoulit.his.pharmacyservice.common.exception.ErrorCode;
import kr.co.seoulit.his.pharmacyservice.inventory.dto.InventoryResponse;
import kr.co.seoulit.his.pharmacyservice.inventory.entity.MedicationLot;
import kr.co.seoulit.his.pharmacyservice.inventory.entity.MedicationStock;
import kr.co.seoulit.his.pharmacyservice.inventory.repository.MedicationStockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private MedicationStockRepository medicationStockRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void search_returnsMappedPage() {
        MedicationLot lot = new MedicationLot("MEDICATION-001", "LOT-001",
                LocalDate.of(2027, 12, 31), LocalDate.of(2026, 6, 1), "EA");
        MedicationStock stock = new MedicationStock(lot, "LOCATION-001");
        Pageable pageable = PageRequest.of(0, 20);
        when(medicationStockRepository.search(eq("MEDICATION-001"), any(), any(), any(), any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(stock), pageable, 1));

        Page<InventoryResponse> result = inventoryService.search(
                "MEDICATION-001", null, null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        InventoryResponse response = result.getContent().get(0);
        assertThat(response.medicationId()).isEqualTo("MEDICATION-001");
        assertThat(response.lotNo()).isEqualTo("LOT-001");
        assertThat(response.currentQty()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getDetail_returnsResponse_whenStockExists() {
        MedicationLot lot = new MedicationLot("MEDICATION-001", "LOT-001",
                LocalDate.of(2027, 12, 31), LocalDate.of(2026, 6, 1), "EA");
        MedicationStock stock = new MedicationStock(lot, "LOCATION-001");
        when(medicationStockRepository.findByIdWithLot(stock.getMedicationStockId()))
                .thenReturn(Optional.of(stock));

        InventoryResponse response = inventoryService.getDetail(stock.getMedicationStockId());

        assertThat(response.medicationStockId()).isEqualTo(stock.getMedicationStockId());
        assertThat(response.storageLocationId()).isEqualTo("LOCATION-001");
    }

    @Test
    void getDetail_throwsNotFound_whenStockMissing() {
        when(medicationStockRepository.findByIdWithLot("missing-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.getDetail("missing-id"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.MEDICATION_STOCK_NOT_FOUND);

        verify(medicationStockRepository).findByIdWithLot("missing-id");
    }
}
