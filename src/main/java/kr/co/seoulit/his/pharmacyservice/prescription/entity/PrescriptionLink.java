package kr.co.seoulit.his.pharmacyservice.prescription.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.co.seoulit.his.pharmacyservice.common.BaseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "PRESCRIPTION_LINK")
public class PrescriptionLink extends BaseEntity {

    @Id
    @Column(name = "PRESCRIPTION_LINK_ID", length = 36)
    private String prescriptionLinkId;

    // 외래(GR2)의 처방전 고유번호. 카프카 이벤트 중복 수신 시 이 값으로 중복 저장을 막는다.
    @Column(name = "PRESCRIPTION_ID", nullable = false, unique = true)
    private String prescriptionId;

    @Column(name = "PATIENT_ID", nullable = false)
    private String patientId;

    @Column(name = "PHYSICIAN_ID", nullable = false)
    private String physicianId;

    @Column(name = "DEPARTMENT_ID", nullable = false)
    private String departmentId;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    // columnDefinition에 DEFAULT를 직접 넣는 이유 — ddl-auto=update가 컬럼을 새로 추가할 때
    // Oracle은 "NOT NULL 컬럼을 DEFAULT 없이 추가"하는 걸 테이블에 기존 row가 있으면 막는다
    // (ORA-01758). DEFAULT 'RECEIVED'를 명시하면 기존 row에도 자동으로 채워지면서 추가되므로,
    // 이미 데이터가 쌓인 운영/개발 DB에서도 스키마 업데이트가 그대로 성공한다.
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20,
            columnDefinition = "VARCHAR2(20 CHAR) DEFAULT 'RECEIVED' NOT NULL CHECK (STATUS IN ('RECEIVED','DISPENSED','REJECTED'))")
    private PrescriptionStatus status = PrescriptionStatus.RECEIVED;

    // 거절 사유. REJECTED 상태일 때만 값이 있다.
    @Column(name = "REJECT_REASON")
    private String rejectReason;

    protected PrescriptionLink() {
    }

    public PrescriptionLink(String prescriptionId, String patientId, String physicianId,
                             String departmentId, LocalDateTime createdAt) {
        this.prescriptionLinkId = UUID.randomUUID().toString();
        this.prescriptionId = prescriptionId;
        this.patientId = patientId;
        this.physicianId = physicianId;
        this.departmentId = departmentId;
        this.createdAt = createdAt;
        this.status = PrescriptionStatus.RECEIVED;
    }

    public void dispense() {
        this.status = PrescriptionStatus.DISPENSED;
    }

    public void reject(String reason) {
        this.status = PrescriptionStatus.REJECTED;
        this.rejectReason = reason;
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

    public PrescriptionStatus getStatus() {
        return status;
    }

    public String getRejectReason() {
        return rejectReason;
    }
}
