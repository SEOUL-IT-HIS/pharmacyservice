package kr.co.seoulit.his.pharmacyservice.prescription.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.co.seoulit.his.pharmacyservice.common.BaseEntity;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "PRESCRIPTION_ITEM_LINK")
public class PrescriptionItemLink extends BaseEntity {

    @Id
    @Column(name = "PRESCRIPTION_ITEM_LINK_ID", length = 36)
    private String prescriptionItemLinkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRESCRIPTION_LINK_ID", nullable = false)
    private PrescriptionLink prescriptionLink;

    // 약제 내부 Medication.medicationId(Long)를 문자열로 저장. 외래는 이 값을 모르므로
    // 이벤트 수신 시 ediCode -> medicationId로 매핑한 뒤 여기에 채워 넣는다.
    @Column(name = "MEDICATION_ID", nullable = false)
    private String medicationId;

    @Column(name = "DOSAGE_QTY", nullable = false)
    private BigDecimal dosageQty;

    @Column(name = "DOSAGE_FORM_CD", nullable = false)
    private String dosageFormCd;

    // 투여횟수/투약일수 — 외래 원본이 문자열이라(예: "1일 2회") 그대로 문자열로 저장한다.
    @Column(name = "FREQUENCY", length = 50)
    private String frequency;

    @Column(name = "DURATION_DAYS", length = 50)
    private String durationDays;

    @Column(name = "DETAIL_INFO", length = 1000)
    private String detailInfo;

    protected PrescriptionItemLink() {
    }

    public PrescriptionItemLink(PrescriptionLink prescriptionLink, String medicationId,
                                 BigDecimal dosageQty, String dosageFormCd,
                                 String frequency, String durationDays, String detailInfo) {
        this.prescriptionItemLinkId = UUID.randomUUID().toString();
        this.prescriptionLink = prescriptionLink;
        this.medicationId = medicationId;
        this.dosageQty = dosageQty;
        this.dosageFormCd = dosageFormCd;
        this.frequency = frequency;
        this.durationDays = durationDays;
        this.detailInfo = detailInfo;
    }

    @Override
    public String getId() {
        return prescriptionItemLinkId;
    }

    public String getPrescriptionItemLinkId() {
        return prescriptionItemLinkId;
    }

    public PrescriptionLink getPrescriptionLink() {
        return prescriptionLink;
    }

    public String getMedicationId() {
        return medicationId;
    }

    public BigDecimal getDosageQty() {
        return dosageQty;
    }

    public String getDosageFormCd() {
        return dosageFormCd;
    }

    public String getFrequency() {
        return frequency;
    }

    public String getDurationDays() {
        return durationDays;
    }

    public String getDetailInfo() {
        return detailInfo;
    }
}
