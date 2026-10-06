package kr.co.seoulit.his.pharmacyservice.issuance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 약품 출고 등록(HL2-8) 요청 — 프론트는 약품ID/수량/처리자를 보낸다 (재고 차감 로트는 서버가 유효기간 빠른 순으로 자동 선택).
 * issuedById는 일반 출고에서는 필수(컨트롤러가 확인)이고, 마약류 출고는 요청 바깥의 staffId가 처리자라 비워서 온다.
 */
public record IssuanceCreateRequest(
        @NotBlank String medicationId,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal quantity,
        String issuedById
) {
}
