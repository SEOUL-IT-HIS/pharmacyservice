package kr.co.seoulit.his.pharmacyservice.medicationreturn.dto;

import kr.co.seoulit.his.pharmacyservice.medicationreturn.entity.MedicationReturnItem;

public record MedicationReturnCreateResponse(String medicationReturnItemId) {

    public static MedicationReturnCreateResponse from(MedicationReturnItem item) {
        return new MedicationReturnCreateResponse(item.getMedicationReturnItemId());
    }
}
