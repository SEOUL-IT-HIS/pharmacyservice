package kr.co.seoulit.his.pharmacyservice.receipt.repository;

import kr.co.seoulit.his.pharmacyservice.receipt.entity.MedicationReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationReceiptItemRepository extends JpaRepository<MedicationReceiptItem, String> {
}
