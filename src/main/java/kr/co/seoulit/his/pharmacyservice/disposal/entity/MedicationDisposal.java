package kr.co.seoulit.his.pharmacyservice.disposal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.co.seoulit.his.pharmacyservice.common.BaseEntity;

import java.time.LocalDate;
import java.util.UUID;

/** 폐기(헤더). 실제 재고 차감 항목은 {@link MedicationDisposalItem}(상세)에 있다. */
@Entity
@Table(name = "MEDICATION_DISPOSAL")
public class MedicationDisposal extends BaseEntity {

    @Id
    @Column(name = "MEDICATION_DISPOSAL_ID", length = 36)
    private String medicationDisposalId;

    @Column(name = "DISPOSAL_DT", nullable = false)
    private LocalDate disposalDt;

    @Column(name = "DISPOSED_BY_ID", nullable = false)
    private String disposedById;

    // DB 컬럼은 20바이트 코드값용(_CD)이다. 아직 프론트가 자유 텍스트로 사유를 받고 있어
    // 20바이트를 넘는 입력은 DisposalService에서 저장 전에 400으로 막는다.
    @Column(name = "DISPOSAL_REASON_CD", nullable = false, length = 200)
    private String disposalReasonCd;

    protected MedicationDisposal() {
    }

    public MedicationDisposal(LocalDate disposalDt, String disposedById, String disposalReasonCd) {
        this.medicationDisposalId = UUID.randomUUID().toString();
        this.disposalDt = disposalDt;
        this.disposedById = disposedById;
        this.disposalReasonCd = disposalReasonCd;
    }

    @Override
    public String getId() {
        return medicationDisposalId;
    }

    public String getMedicationDisposalId() {
        return medicationDisposalId;
    }

    public LocalDate getDisposalDt() {
        return disposalDt;
    }

    public String getDisposedById() {
        return disposedById;
    }

    public String getDisposalReasonCd() {
        return disposalReasonCd;
    }
}
