package kr.co.seoulit.his.pharmacyservice.controlleddrug.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import kr.co.seoulit.his.pharmacyservice.receipt.dto.ReceiptCreateRequest;

import java.util.List;

/** 마약류 입고 관리 — 입고 자체는 기존 ReceiptCreateRequest 그대로, 처리자/입회자만 더 받는다. */
public record ControlledDrugReceiptRequest(
        @Valid @NotNull ReceiptCreateRequest receipt,
        @NotBlank String staffId,
        @NotEmpty List<@NotBlank String> witnessStaffIds
) {
}
