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

/**
 * 조제(헤더). 조제완료 처리 시 처방 1건당 1개 생성된다.
 * 처방항목별 실제 조제 내역은 {@link DispensingItem}(상세)에 있다.
 */
@Entity
@Table(name = "DISPENSING")
public class Dispensing extends BaseEntity {

    public static final String STATUS_DISPENSED = "DISPENSED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    @Id
    @Column(name = "DISPENSING_ID", length = 36)
    private String dispensingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRESCRIPTION_LINK_ID", nullable = false)
    private PrescriptionLink prescriptionLink;

    @Column(name = "DISPENSING_DT", nullable = false)
    private LocalDate dispensingDt;

    @Column(name = "DISPENSE_STATUS_CD", nullable = false, length = 20)
    private String dispenseStatusCd;

    protected Dispensing() {
    }

    public Dispensing(PrescriptionLink prescriptionLink, LocalDate dispensingDt) {
        this.dispensingId = UUID.randomUUID().toString();
        this.prescriptionLink = prescriptionLink;
        this.dispensingDt = dispensingDt;
        this.dispenseStatusCd = STATUS_DISPENSED;
    }

    public void cancel() {
        this.dispenseStatusCd = STATUS_CANCELLED;
    }

    @Override
    public String getId() {
        return dispensingId;
    }

    public String getDispensingId() {
        return dispensingId;
    }

    public PrescriptionLink getPrescriptionLink() {
        return prescriptionLink;
    }

    public LocalDate getDispensingDt() {
        return dispensingDt;
    }

    public String getDispenseStatusCd() {
        return dispenseStatusCd;
    }
}
