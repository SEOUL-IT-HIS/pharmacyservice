package kr.co.seoulit.his.pharmacyservice.medicationreturn.controller;

import jakarta.validation.Valid;
import kr.co.seoulit.his.pharmacyservice.common.ApiResponse;
import kr.co.seoulit.his.pharmacyservice.medicationreturn.dto.MedicationReturnCreateRequest;
import kr.co.seoulit.his.pharmacyservice.medicationreturn.dto.MedicationReturnCreateResponse;
import kr.co.seoulit.his.pharmacyservice.medicationreturn.dto.ReturnedMedicationDisposalCreateRequest;
import kr.co.seoulit.his.pharmacyservice.medicationreturn.entity.MedicationReturnItem;
import kr.co.seoulit.his.pharmacyservice.medicationreturn.service.MedicationReturnService;
import kr.co.seoulit.his.pharmacyservice.medicationreturn.service.ReturnedMedicationDisposalService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pharmacy/returns")
public class MedicationReturnController {

    private final MedicationReturnService medicationReturnService;
    private final ReturnedMedicationDisposalService returnedMedicationDisposalService;

    public MedicationReturnController(MedicationReturnService medicationReturnService,
                                       ReturnedMedicationDisposalService returnedMedicationDisposalService) {
        this.medicationReturnService = medicationReturnService;
        this.returnedMedicationDisposalService = returnedMedicationDisposalService;
    }

    /** 반납 처리 (HL2-22) */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MedicationReturnCreateResponse> create(@Valid @RequestBody MedicationReturnCreateRequest request) {
        MedicationReturnItem item = medicationReturnService.create(request);
        return ApiResponse.success(MedicationReturnCreateResponse.from(item));
    }

    /** 반납약품폐기 처리 (HL2-23) */
    @PostMapping("/{medicationReturnItemId}/disposals")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> createDisposal(@PathVariable String medicationReturnItemId,
                                             @Valid @RequestBody ReturnedMedicationDisposalCreateRequest request) {
        returnedMedicationDisposalService.create(medicationReturnItemId, request);
        return ApiResponse.success(null);
    }
}
