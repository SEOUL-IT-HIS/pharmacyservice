package kr.co.seoulit.his.pharmacyservice.issuance.repository;

import kr.co.seoulit.his.pharmacyservice.issuance.entity.MedicationIssue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationIssueRepository extends JpaRepository<MedicationIssue, String> {
}
