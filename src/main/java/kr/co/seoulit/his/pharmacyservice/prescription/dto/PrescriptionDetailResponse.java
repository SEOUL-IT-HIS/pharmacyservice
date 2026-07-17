package kr.co.seoulit.his.pharmacyservice.prescription.dto;

import kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionLink;

import java.time.LocalDateTime;
import java.util.List;

public record PrescriptionDetailResponse(
        String prescriptionLinkId,
        String prescriptionId,
        String patientId,
        String physicianId,
        String departmentId,
        LocalDateTime createdAt,
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
                items
        );
    }
}
