package kr.co.seoulit.his.pharmacyservice.prescription.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.co.seoulit.his.pharmacyservice.common.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "PRESCRIPTION_LINK")
public class PrescriptionLink extends BaseEntity {

    @Id
    @Column(name = "PRESCRIPTION_LINK_ID", length = 36)
    private String prescriptionLinkId;

    @Column(name = "PRESCRIPTION_ID", nullable = false)
    private String prescriptionId;

    @Column(name = "PATIENT_ID", nullable = false)
    private String patientId;

    @Column(name = "PHYSICIAN_ID", nullable = false)
    private String physicianId;

    @Column(name = "DEPARTMENT_ID", nullable = false)
    private String departmentId;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    public PrescriptionLink() {
    }

    @Override
    public String getId() {
        return prescriptionLinkId;
    }

    public String getPrescriptionLinkId() {
        return prescriptionLinkId;
    }

    public String getPrescriptionId() {
        return prescriptionId;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getPhysicianId() {
        return physicianId;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
