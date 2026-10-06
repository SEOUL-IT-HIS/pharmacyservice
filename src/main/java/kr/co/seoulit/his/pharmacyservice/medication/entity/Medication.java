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

    /**
     * 제형 대분류(투약형태코드) — admin 공통코드 DOSAGE_FORM_CD를 그대로 재사용한다
     * ("01" 알약/캡슐·"02" 수액·"03" 주사, {@link kr.co.seoulit.his.pharmacyservice.prescription.dto.DosageFormCode} 참고).
     * 외래/입원/응급 처방 화면의 드롭다운과 같은 코드그룹이라 새 그룹을 만들지 않았다.
     *
     * FORM_CODE_NAME(제형, 공공API 원문 자유텍스트 — 예: "경질캡슐")과는 역할이 다르다:
     * FORM_CODE_NAME은 상세 설명, DOSAGE_FORM_CD는 재고·처방 화면에서 "주사약만 보기" 같은
     * 필터링에 쓸 수 있는 구조화된 분류값이다.
     *
     * 2026-10-06 이전에 공공API로 적재된 기존 행은 전부 알약/캡슐이라("낱알식별정보" API 자체가
     * 경구 정제·캡슐 전용) 마이그레이션 시 전부 "01"로 채워도 안전하다.
     */
    @Column(name = "DOSAGE_FORM_CD", length = 2)
    private String dosageFormCd;

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
