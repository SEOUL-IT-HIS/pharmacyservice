package kr.co.seoulit.his.pharmacyservice.medicationreturn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.co.seoulit.his.pharmacyservice.common.BaseEntity;
import kr.co.seoulit.his.pharmacyservice.release.entity.MedicationRelease;

import java.time.LocalDate;
import java.util.UUID;

/** 반납(헤더) — 불출된 약을 환자/병동이 다시 가져온 것. 실제 반납 항목은 {@link MedicationReturnItem}에 있다. */
@Entity
@Table(name = "MEDICATION_RETURN")
public class MedicationReturn extends BaseEntity {

    @Id
    @Column(name = "MEDICATION_RETURN_ID", length = 36)
    private String medicationReturnId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEDICATION_RELEASE_ID", nullable = false)
    private MedicationRelease medicationRelease;

    @Column(name = "RETURN_DT", nullable = false)
    private LocalDate returnDt;

    // DB 컬럼은 20바이트 코드값용(_CD)이다. 서비스에서 저장 전에 길이를 검증한다.
    @Column(name = "RETURN_REASON_CD", nullable = false, length = 20)
    private String returnReasonCd;

    @Column(name = "RETURNED_BY_ID", nullable = false)
    private String returnedById;

    protected MedicationReturn() {
    }

    public MedicationReturn(MedicationRelease medicationRelease, LocalDate returnDt, String returnReasonCd, String returnedById) {
        this.medicationReturnId = UUID.randomUUID().toString();
        this.medicationRelease = medicationRelease;
        this.returnDt = returnDt;
        this.returnReasonCd = returnReasonCd;
        this.returnedById = returnedById;
    }

    @Override
    public String getId() {
        return medicationReturnId;
    }

    public String getMedicationReturnId() {
        return medicationReturnId;
    }

    public MedicationRelease getMedicationRelease() {
        return medicationRelease;
    }

    public LocalDate getReturnDt() {
        return returnDt;
    }

    public String getReturnReasonCd() {
        return returnReasonCd;
    }

    public String getReturnedById() {
        return returnedById;
    }
}
