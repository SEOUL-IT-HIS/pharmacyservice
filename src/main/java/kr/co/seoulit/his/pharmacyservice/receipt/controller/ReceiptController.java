package kr.co.seoulit.his.pharmacyservice.receipt.controller;

import jakarta.validation.Valid;
import kr.co.seoulit.his.pharmacyservice.common.ApiResponse;
import kr.co.seoulit.his.pharmacyservice.receipt.dto.ReceiptCreateRequest;
import kr.co.seoulit.his.pharmacyservice.receipt.dto.ReceiptCreateResponse;
import kr.co.seoulit.his.pharmacyservice.receipt.dto.ReceiptListResponse;
import kr.co.seoulit.his.pharmacyservice.receipt.service.ReceiptService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacy/receipts")
public class ReceiptController {

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    /** 입고 조회 (HL2-7) */
    @GetMapping
    public ApiResponse<List<ReceiptListResponse>> list() {
        return ApiResponse.success(receiptService.list());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReceiptCreateResponse> createReceipt(@Valid @RequestBody ReceiptCreateRequest request) {
        return ApiResponse.success(receiptService.createReceipt(request));
    }
}
