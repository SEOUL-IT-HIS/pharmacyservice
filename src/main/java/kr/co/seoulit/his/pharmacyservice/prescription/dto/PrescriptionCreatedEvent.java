package kr.co.seoulit.his.pharmacyservice.prescription.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 외래(GR2) -> 약제(PHM) 처방전 생성 페이로드.
 * 실제 운영 경로는 카프카({@code pharmacy.kafka.topic.prescription-created})지만,
 * 같은 스키마로 REST(POST /api/pharmacy/prescriptions)도 열어둬서 Swagger에서 스펙을 볼 수 있게 했다.
 * 두 경로 모두 결국 {@link kr.co.seoulit.his.pharmacyservice.prescription.service.PrescriptionService#save}를
 * 호출하고, prescriptionId 기준 멱등 처리가 있어 중복 호출돼도 안전하다.
 *
 * prescriptionLinkId / prescriptionItemLinkId는 약제 서비스 내부 PK라 외래는 보내지 않는다.
 * 외래가 아는 값(prescriptionId)만 받아서, 약제가 자체적으로 링크 레코드를 생성한다.
 *
 * createdAt: 외래가 실제로 보내는 값에 타임존 오프셋이 붙어있어(예: "...+09:00") OffsetDateTime으로 받는다.
 * DB/엔티티(PrescriptionLink)는 오프셋 없는 LocalDateTime을 쓰므로, 저장 시점(PrescriptionService.save)에서
 * offsetDateTime.toLocalDateTime()으로 변환한다(오프셋은 버리고 벽시계 시각만 사용).
 */
public record PrescriptionCreatedEvent(
        @NotBlank String prescriptionId,
        @NotBlank String patientId,
        @NotBlank String physicianId,
        @NotBlank String departmentId,
        @NotNull OffsetDateTime createdAt,
        // 진료구분 OPD/ER/IP. 외래/응급/입원 어디서 온 처방인지. 모르면 null.
        String encounterType,
        // 처방 우선순위 — admin 공통코드 ORDER_PRIORITY_CD 값: 01 STAT / 02 Urgent / 03 Routine. 원값 그대로 저장한다.
        String priorityCode,
        // 구두처방 여부 Y/N. 처방코어가 의사 확정 전에도 보내고 확정 갱신 이벤트는 보내지 않아, 약제는 확정 여부를 알 수 없다.
        String verbalYn,
        @NotEmpty List<@Valid PrescriptionItemEvent> items
) {
}
