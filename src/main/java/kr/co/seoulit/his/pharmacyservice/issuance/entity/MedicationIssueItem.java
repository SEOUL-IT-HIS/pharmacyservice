package kr.co.seoulit.his.pharmacyservice.issuance.entity;

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
@Table(name = "MEDICATION_ISSUE_ITEM")
public class MedicationIssueItem extends BaseEntity {

    @Id
    @Column(name = "MEDICATION_ISSUE_ITEM_ID", length = 36)
    private String medicationIssueItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEDICATION_ISSUE_ID", nullable = false)
    private MedicationIssue medicationIssue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEDICATION_LOT_ID", nullable = false)
    private MedicationLot medicationLot;

    @Column(name = "ISSUE_QTY", nullable = false)
    private BigDecimal issueQty;

    protected MedicationIssueItem() {
    }

    public MedicationIssueItem(MedicationIssue medicationIssue, MedicationLot medicationLot, BigDecimal issueQty) {
        this.medicationIssueItemId = UUID.randomUUID().toString();
        this.medicationIssue = medicationIssue;
        this.medicationLot = medicationLot;
        this.issueQty = issueQty;
    }

    @Override
    public String getId() {
        return medicationIssueItemId;
    }

    public String getMedicationIssueItemId() {
        return medicationIssueItemId;
    }

    public MedicationIssue getMedicationIssue() {
        return medicationIssue;
    }

    public MedicationLot getMedicationLot() {
        return medicationLot;
    }

    public BigDecimal getIssueQty() {
        return issueQty;
    }
}
