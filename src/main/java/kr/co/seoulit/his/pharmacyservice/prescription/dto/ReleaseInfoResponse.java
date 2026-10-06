package kr.co.seoulit.his.pharmacyservice.prescription.dto;

import kr.co.seoulit.his.pharmacyservice.release.entity.MedicationRelease;

import java.time.LocalDateTime;

/** 처방전 상세 화면에서 불출 섹션을 그리기 위한 요약 정보. */
public record ReleaseInfoResponse(
        String medicationReleaseId,
        String recipientTypeCd,
        String releaseStatusCd,
        LocalDateTime releasedAt,
        String releasedById,
        String receivedById,
        String guardianName
) {

    public static ReleaseInfoResponse from(MedicationRelease release) {
        return new ReleaseInfoResponse(
                release.getMedicationReleaseId(),
                release.getRecipientTypeCd(),
                release.getReleaseStatusCd(),
                release.getReleasedAt(),
                release.getReleasedById(),
                release.getReceivedById(),
                release.getGuardianName()
        );
    }
}
