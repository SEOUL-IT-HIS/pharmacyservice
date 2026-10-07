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

    public static final String CANCEL_OUTCOME_APPLIED = "APPLIED";
    public static final String CANCEL_OUTCOME_REFUSED = "REFUSED";

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
    // NOT NULL/CHECK는 여기 쓰지 않는다 — Hibernate가 nullable=false와 enum 값으로 뒤에 직접 붙이므로,
    // 같이 쓰면 "NOT NULL ... not null"로 중복돼 Oracle이 ORA-02258로 거부한다.
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20,
            columnDefinition = "VARCHAR2(20 CHAR) DEFAULT 'RECEIVED'")
    private PrescriptionStatus status = PrescriptionStatus.RECEIVED;

    // 거절 사유. REJECTED 상태일 때만 값이 있다.
    @Column(name = "REJECT_REASON")
    private String rejectReason;

    // 조제거절을 처리한 직원 ID. 이 컬럼을 추가하기 전의 거절 건은 null이다.
    @Column(name = "REJECTED_BY_ID", length = 100)
    private String rejectedById;

    // 처방코어가 처방을 취소한 사유/취소자. CANCELLED 상태일 때만 값이 있다.
    @Column(name = "CANCEL_REASON", length = 200)
    private String cancelReason;

    @Column(name = "CANCELLED_BY_ID", length = 100)
    private String cancelledById;

    // 처방코어의 취소 통보를 받은 시각과 처리 결과(CANCEL_OUTCOME_APPLIED/REFUSED). 통보를 받은 적 없으면 둘 다 null.
    @Column(name = "CANCEL_REQUESTED_AT")
    private LocalDateTime cancelRequestedAt;

    @Column(name = "CANCEL_OUTCOME", length = 20)
    private String cancelOutcome;

    // 처방 출처/긴급도/구두 여부. 이 컬럼을 추가하기 전의 처방이거나 보내지 않은 경우 null.
    @Column(name = "ENCOUNTER_TYPE", length = 10)
    private String encounterType;

    @Column(name = "PRIORITY_CODE", length = 10)
    private String priorityCode;

    @Column(name = "VERBAL_YN", length = 1)
    private String verbalYn;

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

    public void applyOrderMeta(String encounterType, String priorityCode, String verbalYn) {
        this.encounterType = encounterType;
        this.priorityCode = priorityCode;
        this.verbalYn = verbalYn;
    }

    public void dispense() {
        this.status = PrescriptionStatus.DISPENSED;
    }

    public void reject(String reason, String rejectedById) {
        this.status = PrescriptionStatus.REJECTED;
        this.rejectReason = reason;
        this.rejectedById = rejectedById;
    }

    /** 조제취소 — 다시 RECEIVED로 되돌려 재처리를 받을 수 있게 한다. */
    public void backToReceived() {
        this.status = PrescriptionStatus.RECEIVED;
    }

    /** 처방코어의 처방 취소 통보 반영. 종결 상태라 다시 RECEIVED로 돌아가지 않는다. */
    public void cancel(String reason, String cancelledById) {
        this.status = PrescriptionStatus.CANCELLED;
        this.cancelReason = reason;
        this.cancelledById = cancelledById;
        this.cancelRequestedAt = LocalDateTime.now();
        this.cancelOutcome = CANCEL_OUTCOME_APPLIED;
    }

    /**
     * 취소 통보를 받았지만 불출이 끝나 반영하지 못한 경우. 상태는 그대로 두고 통보 사실만 남겨
     * 약사가 화면에서 확인할 수 있게 한다.
     */
    public void markCancelRefused(String reason, String cancelledById) {
        this.cancelReason = reason;
        this.cancelledById = cancelledById;
        this.cancelRequestedAt = LocalDateTime.now();
        this.cancelOutcome = CANCEL_OUTCOME_REFUSED;
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

    public String getRejectedById() {
        return rejectedById;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public String getCancelledById() {
        return cancelledById;
    }

    public LocalDateTime getCancelRequestedAt() {
        return cancelRequestedAt;
    }

    public String getCancelOutcome() {
        return cancelOutcome;
    }

    public String getEncounterType() {
        return encounterType;
    }

    public String getPriorityCode() {
        return priorityCode;
    }

    public String getVerbalYn() {
        return verbalYn;
    }
}
