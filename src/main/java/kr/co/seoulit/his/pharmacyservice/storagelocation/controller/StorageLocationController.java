package kr.co.seoulit.his.pharmacyservice.storagelocation.controller;

import jakarta.validation.Valid;
import kr.co.seoulit.his.pharmacyservice.common.ApiResponse;
import kr.co.seoulit.his.pharmacyservice.storagelocation.dto.StorageLocationDto;
import kr.co.seoulit.his.pharmacyservice.storagelocation.dto.StorageLocationRegisterRequest;
import kr.co.seoulit.his.pharmacyservice.storagelocation.service.StorageLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacy/admin/storage-locations")
@RequiredArgsConstructor
public class StorageLocationController {

    private final StorageLocationService storageLocationService;

    /** 보관위치 목록 조회 — 입고 등록 화면의 보관위치 선택 드롭다운이 사용 */
    @GetMapping("/list")
    public ApiResponse<List<StorageLocationDto>> getStorageLocationList() {
        return ApiResponse.success(storageLocationService.getStorageLocationList());
    }

    /** 신규 보관위치 등록 */
    @PostMapping("/register")
    public ApiResponse<Void> registerStorageLocation(@Valid @RequestBody StorageLocationRegisterRequest request) {
        storageLocationService.registerStorageLocation(request);
        return ApiResponse.success(null);
    }
}
