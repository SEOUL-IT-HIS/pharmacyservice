package kr.co.seoulit.his.pharmacyservice.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    DUPLICATE_RECEIPT_ITEM(HttpStatus.BAD_REQUEST, "동일 요청 내에 medicationId와 lotNo가 중복되었습니다."),
    MEDICATION_STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "약품 재고 정보를 찾을 수 없습니다."),
    PRESCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "처방전 정보를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}
