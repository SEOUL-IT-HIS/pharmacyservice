package kr.co.seoulit.his.pharmacyservice.release.repository;

import kr.co.seoulit.his.pharmacyservice.release.entity.MedicationRelease;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicationReleaseRepository extends JpaRepository<MedicationRelease, String> {

    Optional<MedicationRelease> findByDispensing_DispensingIdAndReleaseStatusCd(String dispensingId, String releaseStatusCd);
}
