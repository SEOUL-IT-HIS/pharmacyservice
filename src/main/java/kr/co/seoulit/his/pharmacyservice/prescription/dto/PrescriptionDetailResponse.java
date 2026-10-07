package kr.co.seoulit.his.pharmacyservice.prescription.dto;

import kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionLink;
import kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionStatus;

import java.time.LocalDateTime;
import java.util.List;

public record PrescriptionDetailResponse(
        String prescriptionLinkId,
        String prescriptionId,
        String patientId,
        String physicianId,
        String departmentId,
        LocalDateTime createdAt,
        PrescriptionStatus status,
        String rejectReason,
        List<PrescriptionItemResponse> items,
        // 가장 최근 조제(Dispensing) 건의 불출 상태. 조제 전이거나 아직 불출하지 않았으면 null.
        ReleaseInfoResponse release,
        // 조제완료를 처리한 약사의 직원 ID. 조제 전이거나, 이 컬럼이 생기기 전의 조제건이면 null.
        String dispensedById,
        // 조제거절을 처리한 약사의 직원 ID. 거절 건이 아니거나 이전 데이터면 null.
        String rejectedById,
        // 처방코어 취소 통보 정보. 통보가 없었으면 모두 null. cancelOutcome: APPLIED(반영됨) / REFUSED(불출 이후라 미반영).
        String cancelReason,
        String cancelledById,
        LocalDateTime cancelRequestedAt,
        String cancelOutcome,
        // 처방 출처(OPD/ER/IP), 우선순위(01 STAT/02 Urgent/03 Routine), 구두처방 여부(Y/N). 보내지 않았으면 null.
        String encounterType,
        String priorityCode,
        String verbalYn
) {

    public static PrescriptionDetailResponse from(PrescriptionLink link, List<PrescriptionItemResponse> items,
                                                   ReleaseInfoResponse release, String dispensedById) {
        return new PrescriptionDetailResponse(
                link.getPrescriptionLinkId(),
                link.getPrescriptionId(),
                link.getPatientId(),
                link.getPhysicianId(),
                link.getDepartmentId(),
                link.getCreatedAt(),
                link.getStatus(),
                link.getRejectReason(),
                items,
                release,
                dispensedById,
                link.getRejectedById(),
                link.getCancelReason(),
                link.getCancelledById(),
                link.getCancelRequestedAt(),
                link.getCancelOutcome(),
                link.getEncounterType(),
                link.getPriorityCode(),
                link.getVerbalYn()
        );
    }
}
