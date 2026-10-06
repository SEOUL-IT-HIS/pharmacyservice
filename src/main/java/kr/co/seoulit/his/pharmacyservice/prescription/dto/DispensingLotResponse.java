package kr.co.seoulit.his.pharmacyservice.prescription.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 처방항목 하나가 조제될 때 실제로 사용된 로트 한 줄 — 재고가 모자라 여러 로트로 나뉘면 여러 줄이 된다. */
public record DispensingLotResponse(
        String dispensingItemId,
        String lotNo,
        LocalDate expirationDt,
        BigDecimal dispensedQty,
        // 이미 반납된 수량 — 반납 입력에서 남은 반납 가능 수량을 계산하는 데 쓴다
        BigDecimal returnedQty
) {
}
