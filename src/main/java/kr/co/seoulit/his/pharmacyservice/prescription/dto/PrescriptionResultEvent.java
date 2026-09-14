package kr.co.seoulit.his.pharmacyservice.prescription.dto;

import kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionStatus;

import java.time.OffsetDateTime;

/**
 * 약제(PHM) -> 처방코어(외래) + 응급/병동 조제결과 이벤트 페이로드.
 * 처방코어는 자기 DB 갱신용으로, 응급/병동은 화면 알림용으로 각자 독립 구독한다(2026-09-11 합의).
 *
 * patientId를 넣어두는 건 응급/병동이 "내 환자 건인지" 필터링할 값이 필요해서다.
 */
public record PrescriptionResultEvent(
        String prescriptionId,
        String patientId,
        String physicianId,
        String departmentId,
        PrescriptionStatus status,
        String rejectReason,
        OffsetDateTime occurredAt
) {
}
