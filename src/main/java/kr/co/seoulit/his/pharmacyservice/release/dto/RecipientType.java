package kr.co.seoulit.his.pharmacyservice.release.dto;

import java.util.Arrays;

/**
 * 불출 수령자 유형. ADM 공통코드에 아직 등록 전이라 서버에서 직접 검증한다
 * (DosageFormCode와 동일한 패턴 — {@link kr.co.seoulit.his.pharmacyservice.prescription.dto.DosageFormCode} 참고).
 */
public enum RecipientType {
    /** 환자 본인 */
    PATIENT,
    /** 보호자 */
    GUARDIAN,
    /** 병동(입원) */
    WARD;

    public static boolean isValid(String code) {
        if (code == null) {
            return false;
        }
        return Arrays.stream(values()).anyMatch(v -> v.name().equals(code));
    }
}
