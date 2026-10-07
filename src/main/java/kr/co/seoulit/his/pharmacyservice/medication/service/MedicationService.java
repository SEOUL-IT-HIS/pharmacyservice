package kr.co.seoulit.his.pharmacyservice.medication.service;

import kr.co.seoulit.his.pharmacyservice.medication.dto.MedicationDto;
import kr.co.seoulit.his.pharmacyservice.medication.dto.MedicationRegisterRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MedicationService {

    List<MedicationDto> getMedicationList();

    /** 외래 처방전 작성 화면용 — 약품명 일부로 검색(자동완성) */
    List<MedicationDto> search(String medicationName);

    /** 타 서비스(응급 등) 약품 선택 목록용 — 이름이 비어도 전체를 페이지로 조회한다. ediCodeOnly면 EDI 코드가 있는 약품만. */
    Page<MedicationDto> searchPage(String medicationName, boolean ediCodeOnly, Pageable pageable);

    void registerMedication(MedicationRegisterRequest request);

    /** 공공API(의약품 낱알식별정보)에서 약품 정보를 가져와 ITEM_SEQ 기준으로 저장/갱신한다. */
    int importFromPublicApi();
}
