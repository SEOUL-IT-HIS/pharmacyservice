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

    protected MedicationRelease() {
    }

    public MedicationRelease(Dispensing dispensing, LocalDateTime releasedAt, String recipientTypeCd) {
        this.medicationReleaseId = UUID.randomUUID().toString();
        this.dispensing = dispensing;
        this.releasedAt = releasedAt;
        this.recipientTypeCd = recipientTypeCd;
        this.releaseStatusCd = STATUS_RELEASED;
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
}
