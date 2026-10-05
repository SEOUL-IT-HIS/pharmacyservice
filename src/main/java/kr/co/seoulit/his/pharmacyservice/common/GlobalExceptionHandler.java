package kr.co.seoulit.his.pharmacyservice.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        return ResponseEntity.status(errorCode.getCode()).body(ApiResponse.error(errorCode));
    }

    // @Valid 검증 실패(필수값 누락 등)는 이게 없으면 catch-all(Exception)에 잡혀서 500으로 잘못 나간다.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("요청 검증 실패: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ErrorCode.BAD_REQUEST));
    }

    // 필수 쿼리파라미터 누락(예: GET /medications 에 name 없이 호출)도 400으로 내려준다.
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException ex) {
        log.warn("필수 파라미터 누락: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ErrorCode.BAD_REQUEST));
    }

    // JSON 본문 파싱 실패(깨진 날짜 형식, 숫자 자리에 문자 등)도 400으로 내려준다.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableBody(HttpMessageNotReadableException ex) {
        log.warn("요청 본문 파싱 실패: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ErrorCode.BAD_REQUEST));
    }

    // BusinessException이 아닌 나머지 모든 예외(NPE 등)를 여기서 잡는다.
    // 콘솔엔 log.error로 스택트레이스가 그대로 남으니 디버깅엔 문제 없고,
    // 프론트는 최소한 {code, message} 형태의 정상적인 에러 응답을 받게 된다.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR));
    }
}
