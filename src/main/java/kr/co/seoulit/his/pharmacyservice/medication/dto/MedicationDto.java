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
    private String chart;
    private LocalDate itemPermitDate;
    private String ediCode;
    private String stdCd;
}
