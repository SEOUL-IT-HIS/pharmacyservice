package kr.co.seoulit.his.pharmacyservice.disposal.entity;

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

@Entity
@Table(name = "MEDICATION_DISPOSAL_ITEM")
public class MedicationDisposalItem extends BaseEntity {

    @Id
    @Column(name = "MEDICATION_DISPOSAL_ITEM_ID", length = 36)
    private String medicationDisposalItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEDICATION_DISPOSAL_ID", nullable = false)
    private MedicationDisposal medicationDisposal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEDICATION_LOT_ID", nullable = false)
    private MedicationLot medicationLot;

    @Column(name = "DISPOSAL_QTY", nullable = false)
    private BigDecimal disposalQty;

    protected MedicationDisposalItem() {
    }

    public MedicationDisposalItem(MedicationDisposal medicationDisposal, MedicationLot medicationLot, BigDecimal disposalQty) {
        this.medicationDisposalItemId = UUID.randomUUID().toString();
        this.medicationDisposal = medicationDisposal;
        this.medicationLot = medicationLot;
        this.disposalQty = disposalQty;
    }

    @Override
    public String getId() {
        return medicationDisposalItemId;
    }

    public String getMedicationDisposalItemId() {
        return medicationDisposalItemId;
    }

    public MedicationDisposal getMedicationDisposal() {
        return medicationDisposal;
    }

    public MedicationLot getMedicationLot() {
        return medicationLot;
    }

    public BigDecimal getDisposalQty() {
        return disposalQty;
    }
}
