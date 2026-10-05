package kr.co.seoulit.his.pharmacyservice.prescription.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.co.seoulit.his.pharmacyservice.common.BaseEntity;
import kr.co.seoulit.his.pharmacyservice.inventory.entity.MedicationLot;

import java.math.BigDecimal;
import java.util.UUID;

/** 조제 상세 — 처방항목(PrescriptionItemLink) 하나당 어느 로트에서 얼마나 조제했는지 기록한다. */
@Entity
@Table(name = "DISPENSING_ITEM")
public class DispensingItem extends BaseEntity {

    @Id
    @Column(name = "DISPENSING_ITEM_ID", length = 36)
    private String dispensingItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DISPENSING_ID", nullable = false)
    private Dispensing dispensing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRESCRIPTION_ITEM_LINK_ID", nullable = false)
    private PrescriptionItemLink prescriptionItemLink;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEDICATION_LOT_ID", nullable = false)
    private MedicationLot medicationLot;

    @Column(name = "PRESCRIBED_QTY", nullable = false)
    private BigDecimal prescribedQty;

    // 조제확인(검수) 수량 — 2인 확인 절차가 아직 화면에 없어 당장은 채우지 않는다(null 허용 컬럼).
    @Column(name = "VERIFIED_QTY")
    private BigDecimal verifiedQty;

    @Column(name = "DISPENSED_QTY", nullable = false)
    private BigDecimal dispensedQty;

    protected DispensingItem() {
    }

    public DispensingItem(Dispensing dispensing, PrescriptionItemLink prescriptionItemLink,
                           MedicationLot medicationLot, BigDecimal prescribedQty, BigDecimal dispensedQty) {
        this.dispensingItemId = UUID.randomUUID().toString();
        this.dispensing = dispensing;
        this.prescriptionItemLink = prescriptionItemLink;
        this.medicationLot = medicationLot;
        this.prescribedQty = prescribedQty;
        this.dispensedQty = dispensedQty;
    }

    @Override
    public String getId() {
        return dispensingItemId;
    }

    public String getDispensingItemId() {
        return dispensingItemId;
    }

    public Dispensing getDispensing() {
        return dispensing;
    }

    public PrescriptionItemLink getPrescriptionItemLink() {
        return prescriptionItemLink;
    }

    public MedicationLot getMedicationLot() {
        return medicationLot;
    }

    public BigDecimal getPrescribedQty() {
        return prescribedQty;
    }

    public BigDecimal getVerifiedQty() {
        return verifiedQty;
    }

    public BigDecimal getDispensedQty() {
        return dispensedQty;
    }
}
