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
        String releaseStatusCd,
        // 처방코어 취소 통보를 받은 시각과 결과(APPLIED 반영됨 / REFUSED 불출 이후라 미반영). 통보가 없었으면 null.
        LocalDateTime cancelRequestedAt,
        String cancelOutcome,
        // 처방 출처(OPD/ER/IP), 우선순위(01 STAT/02 Urgent/03 Routine), 구두처방 여부(Y/N). 보내지 않았으면 null.
        String encounterType,
        String priorityCode,
        String verbalYn
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
                releaseStatusCd,
                link.getCancelRequestedAt(),
                link.getCancelOutcome(),
                link.getEncounterType(),
                link.getPriorityCode(),
                link.getVerbalYn()
        );
    }
}
