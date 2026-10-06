package kr.co.seoulit.his.pharmacyservice.prescription.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * ediCode: 약품을 지정하는 값. 약제 내부 medicationId(Long, auto-increment)는 외래가 알 수 없으므로
 * EDI표준코드로 보내주면 약제 쪽에서 Medication.ediCode로 내부 medicationId를 매핑한다.
 *
 * dosageQty는 "1회 투여량" 기준이다(외래 PrescriptionItemDto.dosage에 대응). 총 조제량은
 * dosageQty x frequency x durationDays로 필요할 때 계산한다.
 *
 * frequency/durationDays는 외래 쪽 필드 타입(String)을 그대로 따른다 — 숫자로 강제 변환하다
 * "1일 2회"처럼 텍스트가 섞여 오면 역직렬화가 깨질 수 있어서다.
 *
 * dosageFormCd: admin 공통코드 DOSAGE_FORM_CD의 3개 값("01" 알약/캡슐·"02" 수액·"03" 주사,
 * {@link DosageFormCode} 참고) 중 하나만 허용한다. 이 값이 수납(billing) 쪽 대표 수가코드와
 * 맞춰지므로 임의 문자열은 저장 시점에 거절된다.
 */
public record PrescriptionItemEvent(
        @NotBlank String ediCode,
        // 로그/화면 표시용. 약품명의 진짜 출처는 ediCode로 매핑되는 우리 Medication 마스터라 별도 저장은 안 함
        String itemName,
        @NotNull @Positive BigDecimal dosageQty,
        @NotBlank String dosageFormCd,
        String frequency,
        String durationDays,
        String detailInfo
) {
}
