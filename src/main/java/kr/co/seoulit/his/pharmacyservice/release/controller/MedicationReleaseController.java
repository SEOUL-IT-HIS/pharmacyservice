package kr.co.seoulit.his.pharmacyservice.release.controller;

import jakarta.validation.Valid;
import kr.co.seoulit.his.pharmacyservice.common.ApiResponse;
import kr.co.seoulit.his.pharmacyservice.release.dto.MedicationReleaseCreateRequest;
import kr.co.seoulit.his.pharmacyservice.release.dto.MedicationReleaseCreateResponse;
import kr.co.seoulit.his.pharmacyservice.release.dto.ReleaseCancelRequest;
import kr.co.seoulit.his.pharmacyservice.release.entity.MedicationRelease;
import kr.co.seoulit.his.pharmacyservice.release.service.MedicationReleaseService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pharmacy/releases")
public class MedicationReleaseController {

    private final MedicationReleaseService medicationReleaseService;

    public MedicationReleaseController(MedicationReleaseService medicationReleaseService) {
        this.medicationReleaseService = medicationReleaseService;
    }

    /** 불출 처리 (HL2-20) */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MedicationReleaseCreateResponse> create(@Valid @RequestBody MedicationReleaseCreateRequest request) {
        MedicationRelease release = medicationReleaseService.create(request);
        return ApiResponse.success(MedicationReleaseCreateResponse.from(release));
    }

    /** 불출 취소 (HL2-21) */
    @PatchMapping("/{medicationReleaseId}/cancel")
    public ApiResponse<Void> cancel(@PathVariable String medicationReleaseId,
                                     @Valid @RequestBody ReleaseCancelRequest request) {
        medicationReleaseService.cancel(medicationReleaseId, request.reason(), request.actorId());
        return ApiResponse.success(null);
    }
}
