package kr.co.seoulit.his.pharmacyservice.release.dto;

import jakarta.validation.constraints.NotBlank;

/** 불출 처리(HL2-20) 요청 — 조제완료된 처방 건을 환자/보호자/병동에 전달했음을 기록한다. */
public record MedicationReleaseCreateRequest(
        @NotBlank String prescriptionLinkId,
        @NotBlank String recipientTypeCd
) {
}
