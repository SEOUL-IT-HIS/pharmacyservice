package kr.co.seoulit.his.pharmacyservice.prescription.controller;

import kr.co.seoulit.his.pharmacyservice.common.response.ApiResponse;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionDetailResponse;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionListResponse;
import kr.co.seoulit.his.pharmacyservice.prescription.service.PrescriptionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pharmacy/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
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
