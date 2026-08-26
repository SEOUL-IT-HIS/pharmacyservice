package kr.co.seoulit.his.pharmacyservice.medication.controller;

import kr.co.seoulit.his.pharmacyservice.common.ApiResponse;
import kr.co.seoulit.his.pharmacyservice.medication.dto.MedicationDto;
import kr.co.seoulit.his.pharmacyservice.medication.dto.MedicationRegisterRequest;
import kr.co.seoulit.his.pharmacyservice.medication.dto.MedicationStockInRequest;
import kr.co.seoulit.his.pharmacyservice.medication.service.MedicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/medications")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService medicationService;

    /** 약품 목록 조회 */
    @GetMapping("/list")
    public ApiResponse<List<MedicationDto>> getMedicationList() {
//        return ApiResponse.success(medicationService.getMedicationList());
        return ApiResponse.success(medicationService.getMedicationList());
    }

    /** 신규 약품 등록 */
    @PostMapping("/register")
    public ApiResponse<Void> registerMedication(@RequestBody MedicationRegisterRequest request) {
        medicationService.registerMedication(request);
        return ApiResponse.success(null);
    }

    /** 공공API(의약품 낱알식별정보)에서 약품 정보를 가져와 저장/갱신 */
    @PostMapping("/import")
    public ApiResponse<Integer> importMedications() {
        return ApiResponse.success(medicationService.importFromPublicApi());
    }

    /** 약품 입고 (1단계: 요청 수신 확인용, 저장 로직 없음) */
    @PostMapping("/stock-in")
    public ApiResponse<Void> stockInMedication(@RequestBody MedicationStockInRequest request) {
        log.info("약품 입고 요청 수신: medicationId={}, quantity={}", request.getMedicationId(), request.getQuantity());
        return ApiResponse.success(null);
    }
}
