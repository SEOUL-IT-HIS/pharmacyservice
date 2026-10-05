package kr.co.seoulit.his.pharmacyservice.prescription.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.co.seoulit.his.pharmacyservice.common.BaseEntity;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "DISPENSING_CANCEL")
public class DispensingCancel extends BaseEntity {

    @Id
    @Column(name = "DISPENSING_CANCEL_ID", length = 36)
    private String dispensingCancelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DISPENSING_ID", nullable = false)
    private Dispensing dispensing;

    @Column(name = "CANCEL_DT", nullable = false)
    private LocalDate cancelDt;

    // DB 컬럼은 20바이트 코드값용(_CD)이다. PrescriptionService에서 저장 전에 길이를 검증한다.
    @Column(name = "CANCEL_REASON_CD", nullable = false, length = 20)
    private String cancelReasonCd;

    @Column(name = "CANCELED_BY_ID", nullable = false)
    private String canceledById;

    protected DispensingCancel() {
    }

    public DispensingCancel(Dispensing dispensing, LocalDate cancelDt, String cancelReasonCd, String canceledById) {
        this.dispensingCancelId = UUID.randomUUID().toString();
        this.dispensing = dispensing;
        this.cancelDt = cancelDt;
        this.cancelReasonCd = cancelReasonCd;
        this.canceledById = canceledById;
    }

    @Override
    public String getId() {
        return dispensingCancelId;
    }

    public String getDispensingCancelId() {
        return dispensingCancelId;
    }

    public Dispensing getDispensing() {
        return dispensing;
    }

    public LocalDate getCancelDt() {
        return cancelDt;
    }

    public String getCancelReasonCd() {
        return cancelReasonCd;
    }

    public String getCanceledById() {
        return canceledById;
    }
}
