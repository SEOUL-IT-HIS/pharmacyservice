package kr.co.seoulit.his.pharmacyservice.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 사용자 노출 에러는 message에 PHM{3자리} 코드를 담아 내려주고, 프론트 messages.ts가 문구로 변환한다 (개발표준가이드 15.2).
    SUCCESS(200, "정상 처리되었습니다."),
    BAD_REQUEST(400, "PHM001"),
    MEDICATION_NOT_FOUND(404, "PHM002"),
    PRESCRIPTION_NOT_FOUND(404, "PHM003"),
    PUBLIC_API_CONNECTION_ERROR(503, "PHM004"),
    PUBLIC_API_RESPONSE_ERROR(502, "PHM005"),
    MEDICATION_STOCK_NOT_FOUND(404, "PHM006"),
    DUPLICATE_RECEIPT_ITEM(400, "PHM007"),
    INSUFFICIENT_STOCK(400, "PHM008"),
    INTERNAL_ERROR(500, "서버 오류가 발생했습니다.");

    private final int code;
    private final String message;
}
