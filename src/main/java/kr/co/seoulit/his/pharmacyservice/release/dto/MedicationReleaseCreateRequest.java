package kr.co.seoulit.his.pharmacyservice.release.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 불출 처리(HL2-20) 요청 — 조제완료된 처방 건을 환자/보호자/병동에 전달했음을 기록한다.
 * releasedById: 불출을 처리한 약사(필수). receiverId: 병동(03)에 불출할 때 약을 받은 병동 직원(병동일 때 필수).
 * guardianName: 보호자(02)에게 불출할 때 보호자 이름(보호자일 때 필수). 환자 본인(01)은 둘 다 필요 없다.
 */
public record MedicationReleaseCreateRequest(
        @NotBlank String prescriptionLinkId,
        @NotBlank String recipientTypeCd,
        @NotBlank String releasedById,
        String receiverId,
        String guardianName
) {
}
