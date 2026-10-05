package kr.co.seoulit.his.pharmacyservice.release.dto;

import kr.co.seoulit.his.pharmacyservice.release.entity.MedicationRelease;

public record MedicationReleaseCreateResponse(String medicationReleaseId) {

    public static MedicationReleaseCreateResponse from(MedicationRelease release) {
        return new MedicationReleaseCreateResponse(release.getMedicationReleaseId());
    }
}
