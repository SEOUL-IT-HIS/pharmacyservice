package kr.co.seoulit.his.pharmacyservice.medicationreturn.repository;

import kr.co.seoulit.his.pharmacyservice.medicationreturn.entity.ReturnedMedicationDisposal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReturnedMedicationDisposalRepository extends JpaRepository<ReturnedMedicationDisposal, String> {

    // DB에 UQ_RETU_MEDI_DISP_1(MEDICATION_RETURN_ITEM_ID) 유니크 제약이 있다 — 반납 상세 하나당
    // 반납약품폐기는 한 번만 할 수 있다(부분폐기를 여러 번 나눠 할 수 없음).
    boolean existsByMedicationReturnItem_MedicationReturnItemId(String medicationReturnItemId);
}
