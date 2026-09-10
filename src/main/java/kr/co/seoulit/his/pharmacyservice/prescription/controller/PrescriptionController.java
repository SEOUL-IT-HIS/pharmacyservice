package kr.co.seoulit.his.pharmacyservice.prescription.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.seoulit.his.pharmacyservice.common.ApiResponse;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionCreatedEvent;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionDetailResponse;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionListResponse;
import kr.co.seoulit.his.pharmacyservice.prescription.service.PrescriptionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Prescription", description = "처방전 조회 및 저장 API")
@RestController
@RequestMapping("/api/pharmacy/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    /**
     * 실제 운영 경로는 카프카({@code opd.pharmacy-order.requested.v1} 토픽)를 통한 비동기 저장이다.
     * 이 REST 엔드포인트는 스펙을 Swagger로 노출하기 위해 같은 저장 로직을 그대로 연결해둔 것으로,
     * prescriptionId 기준 멱등 처리가 있어 카프카로 이미 저장된 건을 다시 호출해도 중복 저장되지 않는다.
     */
    @Operation(
            summary = "처방전 저장 (보여주기용)",
            description = "실제 경로는 카프카입니다."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> create(@Valid @RequestBody PrescriptionCreatedEvent request) {
        prescriptionService.save(request);
        return ApiResponse.success(null);
    }

    @GetMapping
    public ApiResponse<Page<PrescriptionListResponse>> search(
            @RequestParam(required = false) String prescriptionId,
            @RequestParam(required = false) String patientId,
            @RequestParam(required = false) String physicianId,
            @RequestParam(required = false) String departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PrescriptionListResponse> result = prescriptionService.search(
                prescriptionId, patientId, physicianId, departmentId, pageable);
        return ApiResponse.success(result);
    }

    @GetMapping("/{prescriptionLinkId}")
    public ApiResponse<PrescriptionDetailResponse> getDetail(@PathVariable String prescriptionLinkId) {
        return ApiResponse.success(prescriptionService.getDetail(prescriptionLinkId));
    }
}
