package kr.co.seoulit.his.pharmacyservice.medication.repository;

import java.util.Optional;
import kr.co.seoulit.his.pharmacyservice.medication.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationRepository extends JpaRepository<Medication, Long> {

    Optional<Medication> findByItemSeq(String itemSeq);
}
