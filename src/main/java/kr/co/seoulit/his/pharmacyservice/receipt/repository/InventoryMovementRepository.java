package kr.co.seoulit.his.pharmacyservice.receipt.repository;

import kr.co.seoulit.his.pharmacyservice.receipt.entity.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, String> {
}
