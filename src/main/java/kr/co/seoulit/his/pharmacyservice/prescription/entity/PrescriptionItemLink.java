package kr.co.seoulit.his.pharmacyservice.prescription.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.co.seoulit.his.pharmacyservice.common.entity.BaseEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "PRESCRIPTION_ITEM_LINK")
public class PrescriptionItemLink extends BaseEntity {

    @Id
    @Column(name = "PRESCRIPTION_ITEM_LINK_ID", length = 36)
    private String prescriptionItemLinkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRESCRIPTION_LINK_ID", nullable = false)
    private PrescriptionLink prescriptionLink;

    @Column(name = "MEDICATION_ID", nullable = false)
    private String medicationId;

    @Column(name = "DOSAGE_QTY", nullable = false)
    private BigDecimal dosageQty;

    @Column(name = "DOSAGE_FORM_CD", nullable = false)
    private String dosageFormCd;

    protected PrescriptionItemLink() {
    }

    @Override
    public String getId() {
        return prescriptionItemLinkId;
    }

    public String getPrescriptionItemLinkId() {
        return prescriptionItemLinkId;
    }

    public PrescriptionLink getPrescriptionLink() {
        return prescriptionLink;
    }

    public String getMedicationId() {
        return medicationId;
    }

    public BigDecimal getDosageQty() {
        return dosageQty;
    }

    public String getDosageFormCd() {
        return dosageFormCd;
    }
}
