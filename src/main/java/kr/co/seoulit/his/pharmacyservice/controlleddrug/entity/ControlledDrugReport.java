package kr.co.seoulit.his.pharmacyservice.controlleddrug.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.co.seoulit.his.pharmacyservice.common.BaseEntity;

import java.time.LocalDate;
import java.util.UUID;

/**
 * 마약류외부보고. 이번 스프린트의 25개 유즈케이스에는 "외부보고" 자체가 없어 등록 API는
 * 만들지 않고 엔티티/리포지토리만 둔다(지시서 3-6에서 엔티티 설계는 요구했으나, 어느
 * 유즈케이스도 보고서 제출 화면을 요구하지 않아 컨트롤러는 범위 밖으로 남겨둠).
 */
@Entity
@Table(name = "CONTROLLED_DRUG_REPORT")
public class ControlledDrugReport extends BaseEntity {

    @Id
    @Column(name = "CONTROLLED_DRUG_REPORT_ID", length = 36)
    private String controlledDrugReportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONTROLLED_DRUG_RECORD_ID", nullable = false)
    private ControlledDrugRecord controlledDrugRecord;

    @Column(name = "REPORT_TYPE_CD", nullable = false, length = 20)
    private String reportTypeCd;

    @Column(name = "REPORT_DT", nullable = false)
    private LocalDate reportDt;

    @Column(name = "SUBMIT_REASON_CD", length = 20)
    private String submitReasonCd;

    @Column(name = "REPORTED_BY_ID", nullable = false)
    private String reportedById;

    protected ControlledDrugReport() {
    }

    public ControlledDrugReport(ControlledDrugRecord controlledDrugRecord, String reportTypeCd, LocalDate reportDt,
                                 String submitReasonCd, String reportedById) {
        this.controlledDrugReportId = UUID.randomUUID().toString();
        this.controlledDrugRecord = controlledDrugRecord;
        this.reportTypeCd = reportTypeCd;
        this.reportDt = reportDt;
        this.submitReasonCd = submitReasonCd;
        this.reportedById = reportedById;
    }

    @Override
    public String getId() {
        return controlledDrugReportId;
    }

    public String getControlledDrugReportId() {
        return controlledDrugReportId;
    }

    public ControlledDrugRecord getControlledDrugRecord() {
        return controlledDrugRecord;
    }

    public String getReportTypeCd() {
        return reportTypeCd;
    }

    public LocalDate getReportDt() {
        return reportDt;
    }

    public String getSubmitReasonCd() {
        return submitReasonCd;
    }

    public String getReportedById() {
        return reportedById;
    }
}
