package kr.co.seoulit.his.pharmacyservice.prescription.entity;

// 처방전 처리 상태. RECEIVED(접수) -> DISPENSED(조제완료) 또는 REJECTED(거절)로 전이된다.
// CANCELLED는 처방코어(외래)가 처방을 취소했을 때만 되는 종결 상태로, 결과 이벤트는 발행하지 않는다.
public enum PrescriptionStatus {
    RECEIVED,
    DISPENSED,
    REJECTED,
    CANCELLED
}
