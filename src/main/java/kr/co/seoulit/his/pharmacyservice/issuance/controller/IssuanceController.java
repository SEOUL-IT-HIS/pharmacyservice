package kr.co.seoulit.his.pharmacyservice.issuance.controller;

import jakarta.validation.Valid;
import kr.co.seoulit.his.pharmacyservice.common.ApiResponse;
import kr.co.seoulit.his.pharmacyservice.issuance.dto.IssuanceCreateRequest;
import kr.co.seoulit.his.pharmacyservice.issuance.dto.IssuanceListResponse;
import kr.co.seoulit.his.pharmacyservice.issuance.service.IssuanceService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacy/issuances")
public class IssuanceController {

    private final IssuanceService issuanceService;

    public IssuanceController(IssuanceService issuanceService) {
        this.issuanceService = issuanceService;
    }

    /** 출고 조회 (HL2-9) */
    @GetMapping
    public ApiResponse<List<IssuanceListResponse>> list() {
        return ApiResponse.success(issuanceService.list());
    }

    /** 출고 등록 (HL2-8) */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> create(@Valid @RequestBody IssuanceCreateRequest request) {
        issuanceService.create(request);
        return ApiResponse.success(null);
    }
}
