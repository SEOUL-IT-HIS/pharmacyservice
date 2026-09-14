package kr.co.seoulit.his.pharmacyservice.prescription.entity;

// 처방전 처리 상태. RECEIVED(접수) -> DISPENSED(조제완료) 또는 REJECTED(거절)로만 전이된다.
public enum PrescriptionStatus {
    RECEIVED,
    DISPENSED,
    REJECTED
}
