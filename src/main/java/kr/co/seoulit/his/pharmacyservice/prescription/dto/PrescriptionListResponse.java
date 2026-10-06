package kr.co.seoulit.his.pharmacyservice.prescription.dto;

import kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionLink;
import kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionStatus;

import java.time.LocalDateTime;

public record PrescriptionListResponse(
        String prescriptionLinkId,
        String prescriptionId,
        String patientId,
        String physicianId,
        String departmentId,
        LocalDateTime createdAt,
        PrescriptionStatus status,
        // 조제완료(DISPENSED) 건의 불출 상태 — RELEASED(불출됨) / CANCELLED(불출취소됨) / null(아직 불출 전, 또는 조제완료가 아님)
        String releaseStatusCd
) {

    public static PrescriptionListResponse from(PrescriptionLink link, String releaseStatusCd) {
        return new PrescriptionListResponse(
                link.getPrescriptionLinkId(),
                link.getPrescriptionId(),
                link.getPatientId(),
                link.getPhysicianId(),
                link.getDepartmentId(),
                link.getCreatedAt(),
                link.getStatus(),
                releaseStatusCd
        );
    }
}
