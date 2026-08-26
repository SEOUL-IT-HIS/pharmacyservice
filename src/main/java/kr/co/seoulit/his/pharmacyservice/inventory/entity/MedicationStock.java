package kr.co.seoulit.his.pharmacyservice.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.co.seoulit.his.pharmacyservice.common.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "MEDICATION_STOCK")
public class MedicationStock extends BaseEntity {

    @Id
    @Column(name = "MEDICATION_STOCK_ID", length = 36)
    private String medicationStockId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEDICATION_LOT_ID", nullable = false)
    private MedicationLot medicationLot;

    @Column(name = "STORAGE_LOCATION_ID", nullable = false)
    private String storageLocationId;

    @Column(name = "CURRENT_QTY", nullable = false)
    private BigDecimal currentQty;

    @Column(name = "LAST_MOVEMENT_AT")
    private LocalDateTime lastMovementAt;

    protected MedicationStock() {
    }

    public MedicationStock(MedicationLot medicationLot, String storageLocationId) {
        this.medicationStockId = UUID.randomUUID().toString();
        this.medicationLot = medicationLot;
        this.storageLocationId = storageLocationId;
        this.currentQty = BigDecimal.ZERO;
    }

    public BigDecimal increaseQty(BigDecimal qty, LocalDateTime movementAt) {
        BigDecimal beforeQty = this.currentQty;
        this.currentQty = this.currentQty.add(qty);
        this.lastMovementAt = movementAt;
        return beforeQty;
    }

    public BigDecimal decreaseQty(BigDecimal qty, LocalDateTime movementAt) {
        BigDecimal beforeQty = this.currentQty;
        this.currentQty = this.currentQty.subtract(qty);
        this.lastMovementAt = movementAt;
        return beforeQty;
    }

    @Override
    public String getId() {
        return medicationStockId;
    }

    public String getMedicationStockId() {
        return medicationStockId;
    }

    public MedicationLot getMedicationLot() {
        return medicationLot;
    }

    public String getStorageLocationId() {
        return storageLocationId;
    }

    public BigDecimal getCurrentQty() {
        return currentQty;
    }

    public LocalDateTime getLastMovementAt() {
        return lastMovementAt;
    }
}
