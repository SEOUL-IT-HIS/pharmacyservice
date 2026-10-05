package kr.co.seoulit.his.pharmacyservice.medicationreturn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.co.seoulit.his.pharmacyservice.common.BaseEntity;
import kr.co.seoulit.his.pharmacyservice.disposal.entity.MedicationDisposal;

import java.math.BigDecimal;
import java.util.UUID;

/** 반납약품폐기 — 반납된 약 중 재사용하지 않고 폐기하는 부분을 실제 MEDICATION_DISPOSAL과 연결한다. */
@Entity
@Table(name = "RETURNED_MEDICATION_DISPOSAL")
public class ReturnedMedicationDisposal extends BaseEntity {

    @Id
    @Column(name = "RETURNED_MEDICATION_DISPOSAL_ID", length = 36)
    private String returnedMedicationDisposalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEDICATION_RETURN_ITEM_ID", nullable = false)
    private MedicationReturnItem medicationReturnItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEDICATION_DISPOSAL_ID", nullable = false)
    private MedicationDisposal medicationDisposal;

    @Column(name = "DISPOSAL_QTY", nullable = false)
    private BigDecimal disposalQty;

    // DB 컬럼은 20바이트 코드값용(_CD)이다. 서비스에서 저장 전에 길이를 검증한다.
    @Column(name = "DISPOSAL_REASON_CD", nullable = false, length = 20)
    private String disposalReasonCd;

    protected ReturnedMedicationDisposal() {
    }

    public ReturnedMedicationDisposal(MedicationReturnItem medicationReturnItem, MedicationDisposal medicationDisposal,
                                       BigDecimal disposalQty, String disposalReasonCd) {
        this.returnedMedicationDisposalId = UUID.randomUUID().toString();
        this.medicationReturnItem = medicationReturnItem;
        this.medicationDisposal = medicationDisposal;
        this.disposalQty = disposalQty;
        this.disposalReasonCd = disposalReasonCd;
    }

    @Override
    public String getId() {
        return returnedMedicationDisposalId;
    }

    public String getReturnedMedicationDisposalId() {
        return returnedMedicationDisposalId;
    }

    public MedicationReturnItem getMedicationReturnItem() {
        return medicationReturnItem;
    }

    public MedicationDisposal getMedicationDisposal() {
        return medicationDisposal;
    }

    public BigDecimal getDisposalQty() {
        return disposalQty;
    }

    public String getDisposalReasonCd() {
        return disposalReasonCd;
    }
}
