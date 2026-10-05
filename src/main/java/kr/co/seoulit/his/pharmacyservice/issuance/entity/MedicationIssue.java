package kr.co.seoulit.his.pharmacyservice.issuance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.co.seoulit.his.pharmacyservice.common.BaseEntity;

import java.util.UUID;

/** 출고(헤더). 실제 재고 차감 항목은 {@link MedicationIssueItem}(상세)에 있다. */
@Entity
@Table(name = "MEDICATION_ISSUE")
public class MedicationIssue extends BaseEntity {

    @Id
    @Column(name = "MEDICATION_ISSUE_ID", length = 36)
    private String medicationIssueId;

    @Column(name = "ISSUED_BY_ID", nullable = false)
    private String issuedById;

    @Column(name = "ISSUE_TYPE_CD", nullable = false)
    private String issueTypeCd;

    protected MedicationIssue() {
    }

    public MedicationIssue(String issuedById, String issueTypeCd) {
        this.medicationIssueId = UUID.randomUUID().toString();
        this.issuedById = issuedById;
        this.issueTypeCd = issueTypeCd;
    }

    @Override
    public String getId() {
        return medicationIssueId;
    }

    public String getMedicationIssueId() {
        return medicationIssueId;
    }

    public String getIssuedById() {
        return issuedById;
    }

    public String getIssueTypeCd() {
        return issueTypeCd;
    }
}
