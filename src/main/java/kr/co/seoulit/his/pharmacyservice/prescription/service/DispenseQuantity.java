package kr.co.seoulit.his.pharmacyservice.prescription.service;

import kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionItemLink;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 처방항목의 조제 수량 계산. dosageQty는 "1회 투여량"이라 frequency(1일 투여횟수) x durationDays(투약일수)를
 * 곱한 총 조제량이 실제로 차감될 수량이다. 조제완료(실제 차감)와 처방전 상세(예상 수량 표시)가 같은
 * 계산을 쓰도록 이 클래스에 모았다.
 *
 * frequency/durationDays는 외래가 자유 텍스트로 보내는 필드라("1일 3회"처럼 숫자+단위가 섞여 올 수 있음)
 * 신뢰도 있게 통째로 파싱할 수 없다. 한 문자열에 숫자가 정확히 하나만 있을 때만("3", "3회") 그 숫자를
 * 쓰고, 그 외엔 1로 본다(= 그 구간은 곱하지 않음, 못 읽은 값을 추측하는 것보다 안전한 보수적 동작).
 * 그렇게 폴백된 경우는 ambiguous=true로 알려서 화면/로그에서 사람이 확인할 수 있게 한다.
 */
final class DispenseQuantity {

    /** 문자열 전체에 숫자가 정확히 한 덩어리일 때만 매칭 (앞뒤 비숫자 문자는 허용: "3", "3회", "5일") */
    private static final Pattern SINGLE_NUMBER = Pattern.compile("^\\D*(\\d+)\\D*$");

    record Calculation(BigDecimal totalQty, boolean ambiguous) {
    }

    private DispenseQuantity() {
    }

    static Calculation of(PrescriptionItemLink item) {
        int timesPerDay = parseCount(item.getFrequency());
        int days = parseCount(item.getDurationDays());
        BigDecimal totalQty = item.getDosageQty()
                .multiply(BigDecimal.valueOf(timesPerDay))
                .multiply(BigDecimal.valueOf(days));
        boolean ambiguous = isAmbiguous(item.getFrequency()) || isAmbiguous(item.getDurationDays());
        return new Calculation(totalQty, ambiguous);
    }

    private static int parseCount(String text) {
        if (text == null) {
            return 1;
        }
        Matcher matcher = SINGLE_NUMBER.matcher(text.trim());
        if (!matcher.matches()) {
            return 1;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    /** 값은 비어있지 않은데(= 뭔가 보내긴 했는데) 숫자 하나로 못 읽어 1로 폴백되는 경우 */
    private static boolean isAmbiguous(String text) {
        return text != null && !text.isBlank() && !SINGLE_NUMBER.matcher(text.trim()).matches();
    }
}
