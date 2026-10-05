package kr.co.seoulit.his.pharmacyservice.medicationreturn.repository;

import kr.co.seoulit.his.pharmacyservice.medicationreturn.entity.MedicationReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicationReturnItemRepository extends JpaRepository<MedicationReturnItem, String> {

    // 같은 조제상세(DispensingItem)에 대해 이전에 이미 반납된 수량이 있는지 확인할 때 쓴다
    // (한 번에 전량을 안 받고 나눠서 여러 번 반납하는 것도 지원하되, 조제된 수량을 넘을 순 없다).
    List<MedicationReturnItem> findByDispensingItem_DispensingItemId(String dispensingItemId);
}
