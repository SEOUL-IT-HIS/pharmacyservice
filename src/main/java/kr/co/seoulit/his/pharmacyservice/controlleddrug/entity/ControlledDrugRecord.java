package kr.co.seoulit.his.pharmacyservice.controlleddrug.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.co.seoulit.his.pharmacyservice.common.BaseEntity;
import kr.co.seoulit.his.pharmacyservice.receipt.entity.InventoryMovement;

import java.util.UUID;

/**
 * 마약류관리기록(특수약품관리). 입고/출고/폐기 자체는 기존 ReceiptService/IssuanceService/
 * DisposalService가 그대로 처리하고, 이 엔티티는 그 결과로 생긴 재고 이력(InventoryMovement)
 * 하나하나에 "누가 처리했고 입회자가 누구였는지"를 얹는 얇은 기록 레이어다.
 */
@Entity
@Table(name = "CONTROLLED_DRUG_RECORD")
public class ControlledDrugRecord extends BaseEntity {

    @Id
    @Column(name = "CONTROLLED_DRUG_RECORD_ID", length = 36)
    private String controlledDrugRecordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INVENTORY_MOVEMENT_ID", nullable = false)
    private InventoryMovement inventoryMovement;

    @Column(name = "PRESCRIPTION_LINK_ID")
    private String prescriptionLinkId;

    @Column(name = "PRESCRIPTION_ID")
    private String prescriptionId;

    @Column(name = "PATIENT_ID")
    private String patientId;

    @Column(name = "STAFF_ID", nullable = false)
    private String staffId;

    // PHM_STOCK_TX_TYPE(ADM 공통코드) 01=입고/02=출고/03=폐기 값을 그대로 재사용한다.
    @Column(name = "CONTROLLED_TX_CD", nullable = false, length = 20)
    private String controlledTxCd;

    protected ControlledDrugRecord() {
    }

    public ControlledDrugRecord(InventoryMovement inventoryMovement, String staffId, String controlledTxCd) {
        this.controlledDrugRecordId = UUID.randomUUID().toString();
        this.inventoryMovement = inventoryMovement;
        this.staffId = staffId;
        this.controlledTxCd = controlledTxCd;
    }

    @Override
    public String getId() {
        return controlledDrugRecordId;
    }

    public String getControlledDrugRecordId() {
        return controlledDrugRecordId;
    }

    public InventoryMovement getInventoryMovement() {
        return inventoryMovement;
    }

    public String getPrescriptionLinkId() {
        return prescriptionLinkId;
    }

    public String getPrescriptionId() {
        return prescriptionId;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getStaffId() {
        return staffId;
    }

    public String getControlledTxCd() {
        return controlledTxCd;
    }
}
