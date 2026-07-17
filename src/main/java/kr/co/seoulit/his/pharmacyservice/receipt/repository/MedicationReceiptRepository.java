package kr.co.seoulit.his.pharmacyservice.receipt.repository;

import kr.co.seoulit.his.pharmacyservice.receipt.entity.MedicationReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationReceiptRepository extends JpaRepository<MedicationReceipt, String> {
}
