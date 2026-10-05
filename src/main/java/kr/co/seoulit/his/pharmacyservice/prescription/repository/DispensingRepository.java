package kr.co.seoulit.his.pharmacyservice.prescription.repository;

import kr.co.seoulit.his.pharmacyservice.prescription.entity.Dispensing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DispensingRepository extends JpaRepository<Dispensing, String> {

    Optional<Dispensing> findByPrescriptionLink_PrescriptionLinkIdAndDispenseStatusCd(
            String prescriptionLinkId, String dispenseStatusCd);
}
