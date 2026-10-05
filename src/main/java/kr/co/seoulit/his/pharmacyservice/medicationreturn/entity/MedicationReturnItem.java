package kr.co.seoulit.his.pharmacyservice.medicationreturn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.co.seoulit.his.pharmacyservice.common.BaseEntity;
import kr.co.seoulit.his.pharmacyservice.prescription.entity.DispensingItem;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 반납 상세 — 조제 상세(DispensingItem) 하나당 얼마나 반납됐는지, 그 중 얼마가 이미 폐기됐는지 기록한다.
 * disposalQty는 {@link ReturnedMedicationDisposal 반납약품폐기} 처리 때마다 누적된다(DB 제약: disposalQty <= returnQty).
 */
@Entity
@Table(name = "MEDICATION_RETURN_ITEM")
public class MedicationReturnItem extends BaseEntity {

    @Id
    @Column(name = "MEDICATION_RETURN_ITEM_ID", length = 36)
    private String medicationReturnItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEDICATION_RETURN_ID", nullable = false)
    private MedicationReturn medicationReturn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DISPENSING_ITEM_ID", nullable = false)
    private DispensingItem dispensingItem;

    @Column(name = "RETURN_QTY", nullable = false)
    private BigDecimal returnQty;

    @Column(name = "DISPOSAL_QTY", nullable = false)
    private BigDecimal disposalQty;

    protected MedicationReturnItem() {
    }

    public MedicationReturnItem(MedicationReturn medicationReturn, DispensingItem dispensingItem, BigDecimal returnQty) {
        this.medicationReturnItemId = UUID.randomUUID().toString();
        this.medicationReturn = medicationReturn;
        this.dispensingItem = dispensingItem;
        this.returnQty = returnQty;
        this.disposalQty = BigDecimal.ZERO;
    }

    public void addDisposalQty(BigDecimal qty) {
        this.disposalQty = this.disposalQty.add(qty);
    }

    @Override
    public String getId() {
        return medicationReturnItemId;
    }

    public String getMedicationReturnItemId() {
        return medicationReturnItemId;
    }

    public MedicationReturn getMedicationReturn() {
        return medicationReturn;
    }

    public DispensingItem getDispensingItem() {
        return dispensingItem;
    }

    public BigDecimal getReturnQty() {
        return returnQty;
    }

    public BigDecimal getDisposalQty() {
        return disposalQty;
    }
}
