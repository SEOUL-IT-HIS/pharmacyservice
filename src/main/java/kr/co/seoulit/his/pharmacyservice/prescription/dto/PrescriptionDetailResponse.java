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
        String rejectedById
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
                link.getRejectedById()
        );
    }
}
