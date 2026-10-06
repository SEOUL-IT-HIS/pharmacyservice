package kr.co.seoulit.his.pharmacyservice.release.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.co.seoulit.his.pharmacyservice.common.BaseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "RELEASE_CANCEL")
public class ReleaseCancel extends BaseEntity {

    @Id
    @Column(name = "RELEASE_CANCEL_ID", length = 36)
    private String releaseCancelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEDICATION_RELEASE_ID", nullable = false)
    private MedicationRelease medicationRelease;

    @Column(name = "CANCELED_AT", nullable = false)
    private LocalDateTime canceledAt;

    // DB 컬럼은 20바이트 코드값용(_CD)이다. 서비스에서 저장 전에 길이를 검증한다.
    @Column(name = "CANCEL_REASON_CD", nullable = false, length = 200)
    private String cancelReasonCd;

    @Column(name = "CANCELED_BY_ID", nullable = false)
    private String canceledById;

    protected ReleaseCancel() {
    }

    public ReleaseCancel(MedicationRelease medicationRelease, LocalDateTime canceledAt, String cancelReasonCd, String canceledById) {
        this.releaseCancelId = UUID.randomUUID().toString();
        this.medicationRelease = medicationRelease;
        this.canceledAt = canceledAt;
        this.cancelReasonCd = cancelReasonCd;
        this.canceledById = canceledById;
    }

    @Override
    public String getId() {
        return releaseCancelId;
    }

    public String getReleaseCancelId() {
        return releaseCancelId;
    }

    public MedicationRelease getMedicationRelease() {
        return medicationRelease;
    }

    public LocalDateTime getCanceledAt() {
        return canceledAt;
    }

    public String getCancelReasonCd() {
        return cancelReasonCd;
    }

    public String getCanceledById() {
        return canceledById;
    }
}
