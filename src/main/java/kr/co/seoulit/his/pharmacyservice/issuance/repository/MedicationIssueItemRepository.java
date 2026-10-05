package kr.co.seoulit.his.pharmacyservice.issuance.repository;

import kr.co.seoulit.his.pharmacyservice.issuance.entity.MedicationIssueItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationIssueItemRepository extends JpaRepository<MedicationIssueItem, String> {
}
