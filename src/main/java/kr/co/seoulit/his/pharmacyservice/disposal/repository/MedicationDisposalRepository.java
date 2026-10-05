package kr.co.seoulit.his.pharmacyservice.disposal.repository;

import kr.co.seoulit.his.pharmacyservice.disposal.entity.MedicationDisposal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationDisposalRepository extends JpaRepository<MedicationDisposal, String> {
}
