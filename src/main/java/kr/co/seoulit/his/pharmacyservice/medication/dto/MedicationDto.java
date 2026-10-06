package kr.co.seoulit.his.pharmacyservice.medication.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MedicationDto {

    private Long medicationId;
    private String medicationName;
    private String itemSeq;
    private String itemEngName;
    private String entpName;
    private String etcOtcName;
    private String classNo;
    private String className;
    private String formCodeName;
    /** admin 공통코드 DOSAGE_FORM_CD 값("01"/"02"/"03") — 제형 대분류 */
    private String dosageFormCd;
    private String chart;
    private LocalDate itemPermitDate;
    private String ediCode;
    private String stdCd;
}
