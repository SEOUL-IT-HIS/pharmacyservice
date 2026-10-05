package kr.co.seoulit.his.pharmacyservice.controlleddrug.controller;

import jakarta.validation.Valid;
import kr.co.seoulit.his.pharmacyservice.common.ApiResponse;
import kr.co.seoulit.his.pharmacyservice.controlleddrug.dto.ControlledDrugDisposalRequest;
import kr.co.seoulit.his.pharmacyservice.controlleddrug.dto.ControlledDrugIssuanceRequest;
import kr.co.seoulit.his.pharmacyservice.controlleddrug.dto.ControlledDrugReceiptRequest;
import kr.co.seoulit.his.pharmacyservice.controlleddrug.dto.ControlledDrugRecordResponse;
import kr.co.seoulit.his.pharmacyservice.controlleddrug.service.ControlledDrugRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 특수약품(마약류)관리. 입고/출고/폐기 자체는 기존 receipts/issuances/disposals API와
 * 같은 로직(ReceiptService/IssuanceService/DisposalService)을 그대로 타고, 처리자/입회자
 * 기록만 더 남긴다.
 */
@RestController
@RequestMapping("/api/pharmacy/controlled-drugs")
public class ControlledDrugController {

    private final ControlledDrugRecordService controlledDrugRecordService;

    public ControlledDrugController(ControlledDrugRecordService controlledDrugRecordService) {
        this.controlledDrugRecordService = controlledDrugRecordService;
    }

    /** 마약류 입고 관리 */
    @PostMapping("/receipts")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<List<ControlledDrugRecordResponse>> createReceipt(@Valid @RequestBody ControlledDrugReceiptRequest request) {
        return ApiResponse.success(controlledDrugRecordService.recordReceipt(request));
    }

    /** 마약류 출고 관리 */
    @PostMapping("/issuances")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<List<ControlledDrugRecordResponse>> createIssuance(@Valid @RequestBody ControlledDrugIssuanceRequest request) {
        return ApiResponse.success(controlledDrugRecordService.recordIssuance(request));
    }

    /** 마약류 폐기 관리 */
    @PostMapping("/disposals")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<List<ControlledDrugRecordResponse>> createDisposal(@Valid @RequestBody ControlledDrugDisposalRequest request) {
        return ApiResponse.success(controlledDrugRecordService.recordDisposal(request));
    }

    /**
     * 특수약품 기록 조회(전체) / 마약류 입고·출고 조회(controlledTxCd=01|02로 필터) 공용.
     * controlledTxCd는 ADM 공통코드 PHM_STOCK_TX_TYPE 값(01=입고/02=출고/03=폐기)과 같다.
     */
    @GetMapping("/records")
    public ApiResponse<List<ControlledDrugRecordResponse>> records(
            @RequestParam(required = false) String controlledTxCd) {
        return ApiResponse.success(controlledDrugRecordService.findRecords(controlledTxCd));
    }
}
