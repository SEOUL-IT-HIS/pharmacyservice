package kr.co.seoulit.his.pharmacyservice.supplier.controller;

import jakarta.validation.Valid;
import kr.co.seoulit.his.pharmacyservice.common.ApiResponse;
import kr.co.seoulit.his.pharmacyservice.supplier.dto.SupplierDto;
import kr.co.seoulit.his.pharmacyservice.supplier.dto.SupplierRegisterRequest;
import kr.co.seoulit.his.pharmacyservice.supplier.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacy/admin/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    /** 공급처 목록 조회 — 입고 등록 화면의 공급처 선택 드롭다운이 사용 */
    @GetMapping("/list")
    public ApiResponse<List<SupplierDto>> getSupplierList() {
        return ApiResponse.success(supplierService.getSupplierList());
    }

    /** 신규 공급처 등록 */
    @PostMapping("/register")
    public ApiResponse<Void> registerSupplier(@Valid @RequestBody SupplierRegisterRequest request) {
        supplierService.registerSupplier(request);
        return ApiResponse.success(null);
    }
}
