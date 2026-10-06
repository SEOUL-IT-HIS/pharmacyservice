package kr.co.seoulit.his.pharmacyservice.prescription.dto;

import jakarta.validation.constraints.NotBlank;

/** 조제완료 요청 — 처리한 약사(로그인한 직원)의 직원 ID를 함께 받는다. */
public record DispenseRequest(
        @NotBlank String actorId
) {
}
