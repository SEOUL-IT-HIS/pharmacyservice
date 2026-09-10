package kr.co.seoulit.his.pharmacyservice.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.co.seoulit.his.pharmacyservice.common.BaseEntity;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "MEDICATION_LOT")
public class MedicationLot extends BaseEntity {

    @Id
    @Column(name = "MEDICATION_LOT_ID", length = 36)
    private String medicationLotId;

    @Column(name = "MEDICATION_ID", nullable = false)
    private String medicationId;

    @Column(name = "LOT_NO", nullable = false)
    private String lotNo;

    @Column(name = "EXPIRATION_DT", nullable = false)
    private LocalDate expirationDt;

    @Column(name = "MANUFACTURE_DT")
    private LocalDate manufactureDt;

    @Column(name = "UNIT_CD", nullable = false)
    private String unitCd;

    protected MedicationLot() {
    }

    public MedicationLot(String medicationId, String lotNo, LocalDate expirationDt, LocalDate manufactureDt, String unitCd) {
        this.medicationLotId = UUID.randomUUID().toString();
        this.medicationId = medicationId;
        this.lotNo = lotNo;
        this.expirationDt = expirationDt;
        this.manufactureDt = manufactureDt;
        this.unitCd = unitCd;
    }

    @Override
    public String getId() {
        return medicationLotId;
    }

    public String getMedicationLotId() {
        return medicationLotId;
    }

    public String getMedicationId() {
        return medicationId;
    }

    public String getLotNo() {
        return lotNo;
    }

    public LocalDate getExpirationDt() {
        return expirationDt;
    }

    public LocalDate getManufactureDt() {
        return manufactureDt;
    }

    public String getUnitCd() {
        return unitCd;
    }
}
