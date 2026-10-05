package kr.co.seoulit.his.pharmacyservice.disposal.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** 약품 폐기 등록(HL2-10) 요청 — 프론트는 약품ID/수량/사유만 입력한다 (재고 차감 로트는 서버가 유효기간 빠른 순으로 자동 선택) */
public record DisposalCreateRequest(
        @NotBlank String medicationId,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal quantity,
        @NotBlank String reason
) {
}
