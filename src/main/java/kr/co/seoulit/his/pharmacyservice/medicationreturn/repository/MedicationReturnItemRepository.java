package kr.co.seoulit.his.pharmacyservice.medicationreturn.repository;

import kr.co.seoulit.his.pharmacyservice.medicationreturn.entity.MedicationReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationReturnItemRepository extends JpaRepository<MedicationReturnItem, String> {
}
