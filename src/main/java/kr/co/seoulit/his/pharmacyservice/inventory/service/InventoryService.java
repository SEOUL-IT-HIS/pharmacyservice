package kr.co.seoulit.his.pharmacyservice.inventory.service;

import kr.co.seoulit.his.pharmacyservice.common.exception.BusinessException;
import kr.co.seoulit.his.pharmacyservice.common.exception.ErrorCode;
import kr.co.seoulit.his.pharmacyservice.inventory.dto.InventoryResponse;
import kr.co.seoulit.his.pharmacyservice.inventory.entity.MedicationStock;
import kr.co.seoulit.his.pharmacyservice.inventory.repository.MedicationStockRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class InventoryService {

    private final MedicationStockRepository medicationStockRepository;

    public InventoryService(MedicationStockRepository medicationStockRepository) {
        this.medicationStockRepository = medicationStockRepository;
    }

    public Page<InventoryResponse> search(String medicationId, String lotNo, String storageLocationId,
                                           LocalDate expirationFrom, LocalDate expirationTo, Pageable pageable) {
        return medicationStockRepository
                .search(medicationId, lotNo, storageLocationId, expirationFrom, expirationTo, pageable)
                .map(InventoryResponse::from);
    }

    public InventoryResponse getDetail(String medicationStockId) {
        MedicationStock stock = medicationStockRepository.findByIdWithLot(medicationStockId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICATION_STOCK_NOT_FOUND));
        return InventoryResponse.from(stock);
    }
}
