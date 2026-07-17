package kr.co.seoulit.his.pharmacyservice.inventory.repository;

import kr.co.seoulit.his.pharmacyservice.inventory.entity.MedicationLot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicationLotRepository extends JpaRepository<MedicationLot, String> {

    Optional<MedicationLot> findByMedicationIdAndLotNo(String medicationId, String lotNo);
}
