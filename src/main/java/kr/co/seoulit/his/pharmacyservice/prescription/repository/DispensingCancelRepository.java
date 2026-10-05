package kr.co.seoulit.his.pharmacyservice.prescription.repository;

import kr.co.seoulit.his.pharmacyservice.prescription.entity.DispensingCancel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DispensingCancelRepository extends JpaRepository<DispensingCancel, String> {
}
