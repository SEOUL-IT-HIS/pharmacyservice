package kr.co.seoulit.his.pharmacyservice.supplier.service;

import kr.co.seoulit.his.pharmacyservice.supplier.dto.SupplierDto;
import kr.co.seoulit.his.pharmacyservice.supplier.dto.SupplierRegisterRequest;
import kr.co.seoulit.his.pharmacyservice.supplier.entity.Supplier;
import kr.co.seoulit.his.pharmacyservice.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 공급처 마스터 — 입고 등록 화면에서 자유 텍스트로 supplierId를 입력하던 것을 없애고
 * 실제 등록된 공급처만 고를 수 있게 하기 위해 새로 만들었다(2026-10-06).
 */
@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    @Transactional(readOnly = true)
    public List<SupplierDto> getSupplierList() {
        return supplierRepository.findAllByOrderBySupplierNameAsc()
                .stream()
                .map(SupplierService::toDto)
                .toList();
    }

    @Transactional
    public void registerSupplier(SupplierRegisterRequest request) {
        Supplier supplier = new Supplier(
                request.getSupplierName().trim(),
                StringUtils.hasText(request.getContactPhone()) ? request.getContactPhone().trim() : null);
        supplierRepository.save(supplier);
    }

    private static SupplierDto toDto(Supplier supplier) {
        SupplierDto dto = new SupplierDto();
        dto.setSupplierId(supplier.getSupplierId());
        dto.setSupplierName(supplier.getSupplierName());
        dto.setContactPhone(supplier.getContactPhone());
        return dto;
    }
}
