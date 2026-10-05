package kr.co.seoulit.his.pharmacyservice.medicationreturn.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** 반납 처리(HL2-22) 요청 — 조제항목(dispensingItemId) 하나를 대상으로 반납을 받는다(학습용 단순화: 한 번에 한 항목만). */
public record MedicationReturnCreateRequest(
        @NotBlank String dispensingItemId,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal returnQty,
        @NotBlank String reason
) {
}
