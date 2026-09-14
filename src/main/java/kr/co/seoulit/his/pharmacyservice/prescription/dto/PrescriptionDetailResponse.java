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
        List<PrescriptionItemResponse> items
) {

    public static PrescriptionDetailResponse from(PrescriptionLink link, List<PrescriptionItemResponse> items) {
        return new PrescriptionDetailResponse(
                link.getPrescriptionLinkId(),
                link.getPrescriptionId(),
                link.getPatientId(),
                link.getPhysicianId(),
                link.getDepartmentId(),
                link.getCreatedAt(),
                link.getStatus(),
                link.getRejectReason(),
                items
        );
    }
}
