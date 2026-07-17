package kr.co.seoulit.his.pharmacyservice.prescription.repository;

import kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionItemLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionItemLinkRepository extends JpaRepository<PrescriptionItemLink, String> {

    List<PrescriptionItemLink> findByPrescriptionLink_PrescriptionLinkId(String prescriptionLinkId);
}
