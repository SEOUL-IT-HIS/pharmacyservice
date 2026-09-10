package kr.co.seoulit.his.pharmacyservice.receipt.repository;

import kr.co.seoulit.his.pharmacyservice.receipt.entity.MedicationReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MedicationReceiptItemRepository extends JpaRepository<MedicationReceiptItem, String> {

    /** 입고 조회(HL2-7) 화면용 — 약품별 입고 항목 전체를 로트/입고헤더와 함께 조회 */
    @Query("SELECT ri FROM MedicationReceiptItem ri JOIN FETCH ri.medicationLot JOIN FETCH ri.medicationReceipt")
    List<MedicationReceiptItem> findAllWithLot();
}
