package kr.co.seoulit.his.pharmacyservice.receipt.entity;

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
@Table(name = "MEDICATION_RECEIPT_ITEM")
public class MedicationReceiptItem extends BaseEntity {

    @Id
    @Column(name = "MEDICATION_RECEIPT_ITEM_ID", length = 36)
    private String medicationReceiptItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEDICATION_RECEIPT_ID", nullable = false)
    private MedicationReceipt medicationReceipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEDICATION_LOT_ID", nullable = false)
    private MedicationLot medicationLot;

    @Column(name = "RECEIPT_QTY", nullable = false)
    private BigDecimal receiptQty;

    @Column(name = "UNIT_PRICE")
    private BigDecimal unitPrice;

    protected MedicationReceiptItem() {
    }

    public MedicationReceiptItem(MedicationReceipt medicationReceipt, MedicationLot medicationLot,
                                  BigDecimal receiptQty, BigDecimal unitPrice) {
        this.medicationReceiptItemId = UUID.randomUUID().toString();
        this.medicationReceipt = medicationReceipt;
        this.medicationLot = medicationLot;
        this.receiptQty = receiptQty;
        this.unitPrice = unitPrice;
    }

    @Override
    public String getId() {
        return medicationReceiptItemId;
    }

    public String getMedicationReceiptItemId() {
        return medicationReceiptItemId;
    }

    public MedicationReceipt getMedicationReceipt() {
        return medicationReceipt;
    }

    public MedicationLot getMedicationLot() {
        return medicationLot;
    }

    public BigDecimal getReceiptQty() {
        return receiptQty;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
}
