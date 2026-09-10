package kr.co.seoulit.his.pharmacyservice.medication.controller;

import io.swagger.v3.oas.annotations.Operation;
import kr.co.seoulit.his.pharmacyservice.common.ApiResponse;
import kr.co.seoulit.his.pharmacyservice.medication.dto.MedicationDto;
import kr.co.seoulit.his.pharmacyservice.medication.service.MedicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 타 서비스(외래 등) 약품 검색용 API. 내부 관리용 /api/pharmacy/admin/medications와 별도.
 */
@Slf4j
@RestController
@RequestMapping("/api/pharmacy/medications")
public class MedicationSearchController {

    private final MedicationService medicationService;

    public MedicationSearchController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @Operation(
            summary = "약품 이름 검색",
            description = "타 서비스에서 처방전 작성 시 약품을 이름으로 검색합니다. "
                    + "약품명 일부만 입력해도 매칭되는 약품을 최대 20건까지 이름순으로 반환합니다. "
                    + "응답의 ediCode를 처방전 저장 이벤트(카프카)의 약품 식별값으로 그대로 사용하면 됩니다."
    )
    @GetMapping
    public ApiResponse<List<MedicationDto>> search(
            @RequestParam String name
    ) {
        // 외래 쪽에서 "검색해도 안 보인다"는 문의가 반복돼(예: name=타이레놀) 원인 파악용으로 남긴다.
        // 파라미터 인코딩 문제(한글 깨짐)와 "결과 0건"을 로그만 보고 구분할 수 있도록 원본값 그대로 찍는다.
        List<MedicationDto> result = medicationService.search(name);
        log.info("약품 이름 검색. name={}, 매칭건수={}", name, result.size());
        return ApiResponse.success(result);
    }
}
