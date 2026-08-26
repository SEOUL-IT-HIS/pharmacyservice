package kr.co.seoulit.his.pharmacyservice.medication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(schema = "PHARMACY", name = "MEDICATION")
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "medicationSeq")
    @SequenceGenerator(name = "medicationSeq", sequenceName = "MEDICATION_SEQ", allocationSize = 1)
    @Column(name = "MEDICATION_ID")
    private Long medicationId;

    @Column(name = "MEDICATION_NAME")
    private String medicationName;

    /** 공공API(의약품 낱알식별정보) ITEM_SEQ — 품목기준코드. 재적재 시 중복 방지용 유니크 키 */
    @Column(name = "ITEM_SEQ", unique = true, length = 20)
    private String itemSeq;

    @Column(name = "ITEM_ENG_NAME")
    private String itemEngName;

    @Column(name = "ENTP_NAME")
    private String entpName;

    @Column(name = "ETC_OTC_NAME", length = 50)
    private String etcOtcName;

    @Column(name = "CLASS_NO", length = 20)
    private String classNo;

    @Column(name = "CLASS_NAME")
    private String className;

    @Column(name = "FORM_CODE_NAME", length = 100)
    private String formCodeName;

    @Column(name = "CHART", length = 1000)
    private String chart;

    @Column(name = "ITEM_PERMIT_DATE")
    private LocalDate itemPermitDate;

    /** 여러 EDI코드가 콤마로 이어져 내려올 수 있어 넉넉히 잡음 */
    @Column(name = "EDI_CODE", length = 200)
    private String ediCode;

    /** 여러 표준코드(바코드)가 콤마로 이어져 내려올 수 있어 넉넉히 잡음 */
    @Column(name = "STD_CD", length = 1000)
    private String stdCd;

    @CreationTimestamp
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
}
