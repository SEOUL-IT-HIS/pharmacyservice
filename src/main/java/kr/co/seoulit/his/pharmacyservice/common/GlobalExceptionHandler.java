package kr.co.seoulit.his.pharmacyservice.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        return ResponseEntity.status(errorCode.getCode()).body(ApiResponse.error(errorCode));
    }

//    @ExceptionHandler(NoResourceFoundException.class)
//    public ResponseEntity<Void> handleNoResourceFoundException(NoResourceFoundException ex) {
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//    }

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
