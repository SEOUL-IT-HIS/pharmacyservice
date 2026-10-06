package kr.co.seoulit.his.pharmacyservice.medicationreturn.repository;

import kr.co.seoulit.his.pharmacyservice.medicationreturn.entity.MedicationReturn;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationReturnRepository extends JpaRepository<MedicationReturn, String> {

    // 이 불출에 대해 반납이 한 건이라도 기록됐는지 — 반납된 적이 있으면 불출을 취소할 수 없다
    boolean existsByMedicationRelease_MedicationReleaseId(String medicationReleaseId);
}
