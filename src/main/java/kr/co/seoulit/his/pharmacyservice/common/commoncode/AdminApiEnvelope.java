package kr.co.seoulit.his.pharmacyservice.common.commoncode;

/**
 * admin-service 응답 래퍼({code, message, data}) 역직렬화 전용.
 * common/ApiResponse.java는 우리 쪽에서 내보내는 응답을 만드는 용도라 private 생성자+setter가
 * 없어 Jackson이 그대로 역직렬화할 수 없다 — 그래서 받는 쪽은 이 record를 따로 둔다.
 */
record AdminApiEnvelope<T>(int code, String message, T data) {
}
