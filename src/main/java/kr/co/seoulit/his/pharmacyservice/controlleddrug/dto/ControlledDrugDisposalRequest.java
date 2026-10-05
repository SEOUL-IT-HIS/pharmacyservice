package kr.co.seoulit.his.pharmacyservice.controlleddrug.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import kr.co.seoulit.his.pharmacyservice.disposal.dto.DisposalCreateRequest;

import java.util.List;

/** 마약류 폐기 관리 — 폐기 자체는 기존 DisposalCreateRequest 그대로, 처리자/입회자만 더 받는다. */
public record ControlledDrugDisposalRequest(
        @Valid @NotNull DisposalCreateRequest disposal,
        @NotBlank String staffId,
        @NotEmpty List<@NotBlank String> witnessStaffIds
) {
}
