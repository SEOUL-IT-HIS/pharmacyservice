package kr.co.seoulit.his.pharmacyservice.receipt.controller;

import jakarta.validation.Valid;
import kr.co.seoulit.his.pharmacyservice.common.response.ApiResponse;
import kr.co.seoulit.his.pharmacyservice.receipt.dto.ReceiptCreateRequest;
import kr.co.seoulit.his.pharmacyservice.receipt.dto.ReceiptCreateResponse;
import kr.co.seoulit.his.pharmacyservice.receipt.service.ReceiptService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pharmacy/receipts")
public class ReceiptController {

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReceiptCreateResponse> createReceipt(@Valid @RequestBody ReceiptCreateRequest request) {
        return ApiResponse.success(receiptService.createReceipt(request));
    }
}
