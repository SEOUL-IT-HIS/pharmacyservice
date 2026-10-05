package kr.co.seoulit.his.pharmacyservice.prescription.repository;

import kr.co.seoulit.his.pharmacyservice.prescription.entity.DispensingItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DispensingItemRepository extends JpaRepository<DispensingItem, String> {

    List<DispensingItem> findByDispensing_DispensingId(String dispensingId);
}
