package kr.co.seoulit.his.pharmacyservice.controlleddrug.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.co.seoulit.his.pharmacyservice.common.BaseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

/** 마약류입회자. 마약류 취급은 법적으로 2인 이상 확인이 필요해 기록마다 입회자를 남긴다. */
@Entity
@Table(name = "CONTROLLED_DRUG_WITNESS")
public class ControlledDrugWitness extends BaseEntity {

    // ADM 공통코드 PHM_WITNESS_ROLE로 등록돼 있다(01=입회자, 타 MSA와 동일하게 숫자코드로 통일).
    public static final String ROLE_WITNESS = "01";

    @Id
    @Column(name = "CONTROLLED_DRUG_WITNESS_ID", length = 36)
    private String controlledDrugWitnessId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONTROLLED_DRUG_RECORD_ID", nullable = false)
    private ControlledDrugRecord controlledDrugRecord;

    @Column(name = "WITNESS_STAFF_ID", nullable = false)
    private String witnessStaffId;

    @Column(name = "WITNESS_ROLE_CD", nullable = false, length = 20)
    private String witnessRoleCd;

    @Column(name = "WITNESSED_AT", nullable = false)
    private LocalDateTime witnessedAt;

    protected ControlledDrugWitness() {
    }

    public ControlledDrugWitness(ControlledDrugRecord controlledDrugRecord, String witnessStaffId,
                                  String witnessRoleCd, LocalDateTime witnessedAt) {
        this.controlledDrugWitnessId = UUID.randomUUID().toString();
        this.controlledDrugRecord = controlledDrugRecord;
        this.witnessStaffId = witnessStaffId;
        this.witnessRoleCd = witnessRoleCd;
        this.witnessedAt = witnessedAt;
    }

    @Override
    public String getId() {
        return controlledDrugWitnessId;
    }

    public String getControlledDrugWitnessId() {
        return controlledDrugWitnessId;
    }

    public ControlledDrugRecord getControlledDrugRecord() {
        return controlledDrugRecord;
    }

    public String getWitnessStaffId() {
        return witnessStaffId;
    }

    public String getWitnessRoleCd() {
        return witnessRoleCd;
    }

    public LocalDateTime getWitnessedAt() {
        return witnessedAt;
    }
}
