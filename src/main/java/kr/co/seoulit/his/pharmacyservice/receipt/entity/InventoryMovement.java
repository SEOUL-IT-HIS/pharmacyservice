package kr.co.seoulit.his.pharmacyservice.receipt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.co.seoulit.his.pharmacyservice.common.entity.BaseEntity;
import kr.co.seoulit.his.pharmacyservice.inventory.entity.MedicationStock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "INVENTORY_MOVEMENT")
public class InventoryMovement extends BaseEntity {

    @Id
    @Column(name = "INVENTORY_MOVEMENT_ID", length = 36)
    private String inventoryMovementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEDICATION_STOCK_ID", nullable = false)
    private MedicationStock medicationStock;

    @Column(name = "STOCK_TX_TYPE_CD", nullable = false)
    private String stockTxTypeCd;

    @Column(name = "MOVEMENT_QTY", nullable = false)
    private BigDecimal movementQty;

    @Column(name = "BEFORE_QTY", nullable = false)
    private BigDecimal beforeQty;

    @Column(name = "AFTER_QTY", nullable = false)
    private BigDecimal afterQty;

    @Column(name = "SOURCE_FORM_ID", nullable = false)
    private String sourceFormId;

    @Column(name = "SOURCE_FORM_TYPE_CD", nullable = false)
    private String sourceFormTypeCd;

    @Column(name = "MOVEMENT_AT", nullable = false)
    private LocalDateTime movementAt;

    @Column(name = "MOVED_BY_ID", nullable = false)
    private String movedById;

    protected InventoryMovement() {
    }

    public InventoryMovement(MedicationStock medicationStock, String stockTxTypeCd, BigDecimal movementQty,
                              BigDecimal beforeQty, BigDecimal afterQty, String sourceFormId,
                              String sourceFormTypeCd, LocalDateTime movementAt, String movedById) {
        this.inventoryMovementId = UUID.randomUUID().toString();
        this.medicationStock = medicationStock;
        this.stockTxTypeCd = stockTxTypeCd;
        this.movementQty = movementQty;
        this.beforeQty = beforeQty;
        this.afterQty = afterQty;
        this.sourceFormId = sourceFormId;
        this.sourceFormTypeCd = sourceFormTypeCd;
        this.movementAt = movementAt;
        this.movedById = movedById;
    }

    @Override
    public String getId() {
        return inventoryMovementId;
    }

    public String getInventoryMovementId() {
        return inventoryMovementId;
    }

    public MedicationStock getMedicationStock() {
        return medicationStock;
    }

    public String getStockTxTypeCd() {
        return stockTxTypeCd;
    }

    public BigDecimal getMovementQty() {
        return movementQty;
    }

    public BigDecimal getBeforeQty() {
        return beforeQty;
    }

    public BigDecimal getAfterQty() {
        return afterQty;
    }

    public String getSourceFormId() {
        return sourceFormId;
    }

    public String getSourceFormTypeCd() {
        return sourceFormTypeCd;
    }

    public LocalDateTime getMovementAt() {
        return movementAt;
    }

    public String getMovedById() {
        return movedById;
    }
}
