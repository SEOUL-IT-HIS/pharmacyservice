package kr.co.seoulit.his.pharmacyservice.receipt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.co.seoulit.his.pharmacyservice.common.entity.BaseEntity;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "MEDICATION_RECEIPT")
public class MedicationReceipt extends BaseEntity {

    @Id
    @Column(name = "MEDICATION_RECEIPT_ID", length = 36)
    private String medicationReceiptId;

    @Column(name = "SUPPLIER_ID", nullable = false)
    private String supplierId;

    @Column(name = "STORAGE_LOCATION_ID", nullable = false)
    private String storageLocationId;

    @Column(name = "RECEIPT_DT", nullable = false)
    private LocalDate receiptDt;

    @Column(name = "RECEIVED_BY_ID", nullable = false)
    private String receivedById;

    protected MedicationReceipt() {
    }

    public MedicationReceipt(String supplierId, String storageLocationId, LocalDate receiptDt, String receivedById) {
        this.medicationReceiptId = UUID.randomUUID().toString();
        this.supplierId = supplierId;
        this.storageLocationId = storageLocationId;
        this.receiptDt = receiptDt;
        this.receivedById = receivedById;
    }

    @Override
    public String getId() {
        return medicationReceiptId;
    }

    public String getMedicationReceiptId() {
        return medicationReceiptId;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public String getStorageLocationId() {
        return storageLocationId;
    }

    public LocalDate getReceiptDt() {
        return receiptDt;
    }

    public String getReceivedById() {
        return receivedById;
    }
}
