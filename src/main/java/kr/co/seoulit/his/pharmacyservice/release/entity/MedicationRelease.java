package kr.co.seoulit.his.pharmacyservice.release.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.co.seoulit.his.pharmacyservice.common.BaseEntity;
import kr.co.seoulit.his.pharmacyservice.prescription.entity.Dispensing;

import java.time.LocalDateTime;
import java.util.UUID;

/** 불출 — 조제완료(Dispensing)된 약을 환자/병동 등에게 최종 전달하는 처리. */
@Entity
@Table(name = "MEDICATION_RELEASE")
public class MedicationRelease extends BaseEntity {

    public static final String STATUS_RELEASED = "RELEASED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    @Id
    @Column(name = "MEDICATION_RELEASE_ID", length = 36)
    private String medicationReleaseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DISPENSING_ID", nullable = false)
    private Dispensing dispensing;

    @Column(name = "RELEASED_AT", nullable = false)
    private LocalDateTime releasedAt;

    @Column(name = "RECIPIENT_TYPE_CD", nullable = false, length = 20)
    private String recipientTypeCd;

    @Column(name = "RELEASE_STATUS_CD", nullable = false, length = 20)
    private String releaseStatusCd;

    // 불출을 처리한 약사. 이 컬럼들을 추가하기 전에 만들어진 불출건은 null이다.
    @Column(name = "RELEASED_BY_ID", length = 100)
    private String releasedById;

    // 병동(WARD)에 불출할 때 약을 받은 병동 직원(간호사 등)의 직원 ID
    @Column(name = "RECEIVED_BY_ID", length = 100)
    private String receivedById;

    // 보호자(GUARDIAN)에게 불출할 때 받은 보호자 이름. 보호자는 시스템에 등록된 사람이 아니라 직접 입력받는다.
    @Column(name = "GUARDIAN_NAME", length = 100)
    private String guardianName;

    protected MedicationRelease() {
    }

    public MedicationRelease(Dispensing dispensing, LocalDateTime releasedAt, String recipientTypeCd,
                              String releasedById, String receivedById, String guardianName) {
        this.medicationReleaseId = UUID.randomUUID().toString();
        this.dispensing = dispensing;
        this.releasedAt = releasedAt;
        this.recipientTypeCd = recipientTypeCd;
        this.releaseStatusCd = STATUS_RELEASED;
        this.releasedById = releasedById;
        this.receivedById = receivedById;
        this.guardianName = guardianName;
    }

    public void cancel() {
        this.releaseStatusCd = STATUS_CANCELLED;
    }

    @Override
    public String getId() {
        return medicationReleaseId;
    }

    public String getMedicationReleaseId() {
        return medicationReleaseId;
    }

    public Dispensing getDispensing() {
        return dispensing;
    }

    public LocalDateTime getReleasedAt() {
        return releasedAt;
    }

    public String getRecipientTypeCd() {
        return recipientTypeCd;
    }

    public String getReleaseStatusCd() {
        return releaseStatusCd;
    }

    public String getReleasedById() {
        return releasedById;
    }

    public String getReceivedById() {
        return receivedById;
    }

    public String getGuardianName() {
        return guardianName;
    }
}
