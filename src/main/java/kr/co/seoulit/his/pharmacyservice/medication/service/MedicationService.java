package kr.co.seoulit.his.pharmacyservice.medication.service;

import kr.co.seoulit.his.pharmacyservice.medication.dto.MedicationDto;
import kr.co.seoulit.his.pharmacyservice.medication.dto.MedicationRegisterRequest;

import java.util.List;

public interface MedicationService {

    List<MedicationDto> getMedicationList();

    void registerMedication(MedicationRegisterRequest request);

    /** 공공API(의약품 낱알식별정보)에서 약품 정보를 가져와 ITEM_SEQ 기준으로 저장/갱신한다. */
    int importFromPublicApi();
}
