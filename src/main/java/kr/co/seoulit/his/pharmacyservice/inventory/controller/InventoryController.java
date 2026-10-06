package kr.co.seoulit.his.pharmacyservice.inventory.controller;

import kr.co.seoulit.his.pharmacyservice.common.ApiResponse;
import kr.co.seoulit.his.pharmacyservice.inventory.dto.InventoryMovementResponse;
import kr.co.seoulit.his.pharmacyservice.inventory.dto.InventoryResponse;
import kr.co.seoulit.his.pharmacyservice.inventory.service.InventoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pharmacy/inventories")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ApiResponse<Page<InventoryResponse>> search(
            @RequestParam(required = false) String medicationId,
            @RequestParam(required = false) String lotNo,
            @RequestParam(required = false) String storageLocationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expirationFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expirationTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<InventoryResponse> result = inventoryService.search(
                medicationId, lotNo, storageLocationId, expirationFrom, expirationTo, pageable);
        return ApiResponse.success(result);
    }

    @GetMapping("/{medicationStockId}")
    public ApiResponse<InventoryResponse> getDetail(@PathVariable String medicationStockId) {
        return ApiResponse.success(inventoryService.getDetail(medicationStockId));
    }

    /**
     * 재고부족 목록 — 현재 수량이 threshold 이하인 재고.
     * 예: GET /api/pharmacy/inventories/low-stock-list?threshold=100
     */
    @GetMapping("/low-stock-list")
    public ApiResponse<List<InventoryResponse>> findLowStock(@RequestParam int threshold) {
        return ApiResponse.success(inventoryService.findLowStock(threshold));
    }

    /**
     * 약품 상세(품목 중심 워크스페이스)의 "최근 입출고 내역" 패널용.
     * 예: GET /api/pharmacy/inventories/medications/4150/movements?limit=20
     */
    @GetMapping("/medications/{medicationId}/movements")
    public ApiResponse<List<InventoryMovementResponse>> findRecentMovements(
            @PathVariable String medicationId,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.success(inventoryService.findRecentMovements(medicationId, limit));
    }
}
