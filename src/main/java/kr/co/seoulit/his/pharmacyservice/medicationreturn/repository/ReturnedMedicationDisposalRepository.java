package kr.co.seoulit.his.pharmacyservice.medicationreturn.repository;

import kr.co.seoulit.his.pharmacyservice.medicationreturn.entity.ReturnedMedicationDisposal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReturnedMedicationDisposalRepository extends JpaRepository<ReturnedMedicationDisposal, String> {
}
