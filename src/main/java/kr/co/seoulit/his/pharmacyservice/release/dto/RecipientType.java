package kr.co.seoulit.his.pharmacyservice.release.dto;

import java.util.Arrays;

/**
 * 불출 수령자 유형. ADM 공통코드 PHM_RECIPIENT_TYPE으로 등록돼 있다(01=환자/02=보호자/03=병동,
 * 타 MSA와 동일하게 숫자코드로 통일). 여기서는 그 등록값과 어긋나지 않는지 서버 쪽에서
 * 한 번 더 방어적으로 검증하는 용도로 유지한다(DosageFormCode와 동일한 패턴 —
 * {@link kr.co.seoulit.his.pharmacyservice.prescription.dto.DosageFormCode} 참고).
 */
public enum RecipientType {
    /** 환자 본인 */
    PATIENT("01"),
    /** 보호자 */
    GUARDIAN("02"),
    /** 병동(입원) */
    WARD("03");

    private final String code;

    RecipientType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static boolean isValid(String code) {
        if (code == null) {
            return false;
        }
        return Arrays.stream(values()).anyMatch(v -> v.code.equals(code));
    }
}
