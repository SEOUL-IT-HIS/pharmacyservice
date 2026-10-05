package kr.co.seoulit.his.pharmacyservice.disposal.repository;

import kr.co.seoulit.his.pharmacyservice.disposal.entity.MedicationDisposalItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationDisposalItemRepository extends JpaRepository<MedicationDisposalItem, String> {
}
