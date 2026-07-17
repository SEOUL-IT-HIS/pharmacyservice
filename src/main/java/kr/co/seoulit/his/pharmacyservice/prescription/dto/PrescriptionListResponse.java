package kr.co.seoulit.his.pharmacyservice.prescription.dto;

import kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionLink;

import java.time.LocalDateTime;

public record PrescriptionListResponse(
        String prescriptionLinkId,
        String prescriptionId,
        String patientId,
        String physicianId,
        String departmentId,
        LocalDateTime createdAt
) {

    public static PrescriptionListResponse from(PrescriptionLink link) {
        return new PrescriptionListResponse(
                link.getPrescriptionLinkId(),
                link.getPrescriptionId(),
                link.getPatientId(),
                link.getPhysicianId(),
                link.getDepartmentId(),
                link.getCreatedAt()
        );
    }
}
