package kr.co.seoulit.his.pharmacyservice.disposal.controller;

import jakarta.validation.Valid;
import kr.co.seoulit.his.pharmacyservice.common.ApiResponse;
import kr.co.seoulit.his.pharmacyservice.common.BusinessException;
import kr.co.seoulit.his.pharmacyservice.common.ErrorCode;
import kr.co.seoulit.his.pharmacyservice.disposal.dto.DisposalCreateRequest;
import kr.co.seoulit.his.pharmacyservice.disposal.service.DisposalService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pharmacy/disposals")
public class DisposalController {

    private final DisposalService disposalService;

    public DisposalController(DisposalService disposalService) {
        this.disposalService = disposalService;
    }

    /** 폐기 등록 (HL2-10) */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> create(@Valid @RequestBody DisposalCreateRequest request) {
        if (request.disposedById() == null || request.disposedById().isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        disposalService.create(request, request.disposedById());
        return ApiResponse.success(null);
    }
}
