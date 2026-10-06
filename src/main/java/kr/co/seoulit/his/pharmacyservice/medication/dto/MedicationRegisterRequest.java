package kr.co.seoulit.his.pharmacyservice.medication.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MedicationRegisterRequest {

    @NotBlank
    private String medicationName;
    private String itemSeq;
    private String itemEngName;
    private String entpName;
    private String etcOtcName;
    private String classNo;
    private String className;
    private String formCodeName;
    /**
     * 제형 대분류 — admin 공통코드 DOSAGE_FORM_CD 값("01" 알약/캡슐·"02" 수액·"03" 주사) 중 하나.
     * 화면에서 자유 텍스트로 입력받지 않고 드롭다운으로 선택하게 하고, 서버에서도
     * {@link kr.co.seoulit.his.pharmacyservice.prescription.dto.DosageFormCode#isValid}로 재검증한다.
     */
    @NotBlank
    private String dosageFormCd;
    private String chart;
    private LocalDate itemPermitDate;
    private String ediCode;
    private String stdCd;
}
