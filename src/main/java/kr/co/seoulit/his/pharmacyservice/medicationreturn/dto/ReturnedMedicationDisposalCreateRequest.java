package kr.co.seoulit.his.pharmacyservice.medicationreturn.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** 반납약품폐기(HL2-23) 요청 — 반납 상세(medicationReturnItemId)에서 얼마를 폐기할지 받는다. */
public record ReturnedMedicationDisposalCreateRequest(
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal disposalQty,
        @NotBlank String reason,
        @NotBlank String actorId
) {
}
