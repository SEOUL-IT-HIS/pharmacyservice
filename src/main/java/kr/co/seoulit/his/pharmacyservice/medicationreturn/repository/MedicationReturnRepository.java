package kr.co.seoulit.his.pharmacyservice.medicationreturn.repository;

import kr.co.seoulit.his.pharmacyservice.medicationreturn.entity.MedicationReturn;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationReturnRepository extends JpaRepository<MedicationReturn, String> {
}
