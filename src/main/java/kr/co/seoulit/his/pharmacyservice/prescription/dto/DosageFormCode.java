package kr.co.seoulit.his.pharmacyservice.prescription.dto;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * 처방 항목(PrescriptionItemEvent.dosageFormCd)에 허용되는 투약형태코드 3종.
 *
 * 배경(2026-09-04 확정): 실제 약품은 수십~수백 종류지만, 수납(billing) 쪽은 약품 코드별로
 * 개별 수가코드를 부여하지 않고 "대표 항목 몇 가지로 수가코드를 통일"하기로 이미 결정돼 있다
 * (hisfrontend/docs/개발표준가이드.md 21.10절). 그 대표 항목이 이 3가지다 — 알약/캡슐, 수액, 주사.
 * 약제(PHM)가 실제로 계산·전송하는 값은 아니고, 수납 쪽 대표 수가코드와 맞춰 여기 문서화만
 * 해둔다(수납 쪽 실제 수가 반영은 billing-service 소관).
 *
 * [정정 2026-09-08] 원래 이 자리에 "수납-약제 간 계약용 값이라 ADM 공통코드 대상이 아니다"라고
 * 적혀 있었으나 틀린 판단이었다. 개발표준가이드 21.4절(2026-08-31 결정)에 따르면 서비스 전용
 * 코드라도 `_cd`로 끝나는 코드성 컬럼은 사용 서비스 수와 무관하게 예외 없이 admin-service에서
 * 관리해야 한다. dosageFormCd가 정확히 이 케이스라 ADM 공통코드 그룹(DOSAGE_FORM_CD)으로
 * 등록하는 게 맞다 — 외래 처방 작성 화면의 드롭다운도 그 등록 결과를 그대로 참조한다.
 * 이 enum은 등록된 값과 어긋나지 않는지 서버 쪽에서 한 번 더 방어적으로 검증하는 용도로 유지한다.
 *
 * 외래(GR2)는 처방 항목마다 이 3개 코드 중 하나를 반드시 dosageFormCd에 실어 보내야 하고,
 * 그 외 값은 저장 시점에 거절된다({@link kr.co.seoulit.his.pharmacyservice.common.ErrorCode#INVALID_DOSAGE_FORM_CODE}).
 *
 * [정정 2026-10-06] 실제로 admin에 등록된 DOSAGE_FORM_CD 공통코드 값은 "TAB"/"IV"/"INJ" 같은
 * 영문 리터럴이 아니라 숫자코드 "01"(Tablet/Capsule)/"02"(IV Fluid)/"03"(Injection)이다
 * (외래 PrescriptionForm.tsx, 입원 registerForm.tsx, 응급 commonDrugs.ts가 모두 이 숫자코드로
 * 드롭다운을 채우고 그대로 전송함 — hisfrontend 소스 직접 대조). 이 enum이 이전까지
 * isValid()에서 v.name()("TAB" 등)과 비교하고 있어서, 외래/입원/응급이 실제로 보내는 "01"/"02"/"03"
 * 값은 전부 거절되고 있었다(PHM009). enum 이름(TAB/IV/INJ)은 Java 코드 가독성용으로 유지하고,
 * 실제 코드값 비교는 code 필드("01"/"02"/"03")로 한다.
 */
public enum DosageFormCode {

    /** 알약/캡슐약(경구 고형제) — admin DOSAGE_FORM_CD 공통코드값 "01"(Tablet/Capsule) */
    TAB("01", "알약/캡슐약", new BigDecimal("500")),
    /** 수액 — admin DOSAGE_FORM_CD 공통코드값 "02"(IV Fluid) */
    IV("02", "수액", new BigDecimal("1000")),
    /** 주사 — admin DOSAGE_FORM_CD 공통코드값 "03"(Injection) */
    INJ("03", "주사", new BigDecimal("1500"));

    private final String code;
    private final String label;
    /** 참고용 대표 수가 — 실제 청구 금액 계산·확정은 수납(billing-service) 소관이다. */
    private final BigDecimal referenceBillingPrice;

    DosageFormCode(String code, String label, BigDecimal referenceBillingPrice) {
        this.code = code;
        this.label = label;
        this.referenceBillingPrice = referenceBillingPrice;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public BigDecimal getReferenceBillingPrice() {
        return referenceBillingPrice;
    }

    public static boolean isValid(String code) {
        if (code == null) {
            return false;
        }
        return Arrays.stream(values()).anyMatch(v -> v.code.equals(code));
    }

    public static DosageFormCode fromCode(String code) {
        return Arrays.stream(values())
                .filter(v -> v.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
