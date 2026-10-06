package kr.co.seoulit.his.pharmacyservice.prescription.repository;

import kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionLink;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PrescriptionLinkRepository extends JpaRepository<PrescriptionLink, String> {

    // 카프카 이벤트 중복 수신(재전송) 시 이미 저장된 처방전인지 확인하기 위한 조회
    Optional<PrescriptionLink> findByPrescriptionId(String prescriptionId);

    @Query("""
            SELECT p FROM PrescriptionLink p
            WHERE (:prescriptionId IS NULL OR p.prescriptionId = :prescriptionId)
              AND (:patientId IS NULL OR p.patientId = :patientId)
              AND (:physicianId IS NULL OR p.physicianId = :physicianId)
              AND (:departmentId IS NULL OR p.departmentId = :departmentId)
              AND (:stage IS NULL OR :stage = 'ALL'
                   OR (:stage = 'RECEIVED' AND p.status = kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionStatus.RECEIVED)
                   OR (:stage = 'REJECTED' AND p.status = kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionStatus.REJECTED)
                   OR (:stage = 'DISPENSED' AND p.status = kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionStatus.DISPENSED
                       AND NOT EXISTS (SELECT 1 FROM MedicationRelease r JOIN r.dispensing d
                                       WHERE d.prescriptionLink = p AND d.dispenseStatusCd = 'DISPENSED'))
                   OR (:stage = 'RELEASED' AND p.status = kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionStatus.DISPENSED
                       AND EXISTS (SELECT 1 FROM MedicationRelease r JOIN r.dispensing d
                                   WHERE d.prescriptionLink = p AND d.dispenseStatusCd = 'DISPENSED' AND r.releaseStatusCd = 'RELEASED'))
                   OR (:stage = 'RELEASE_CANCELLED' AND p.status = kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionStatus.DISPENSED
                       AND EXISTS (SELECT 1 FROM MedicationRelease r JOIN r.dispensing d
                                   WHERE d.prescriptionLink = p AND d.dispenseStatusCd = 'DISPENSED' AND r.releaseStatusCd = 'CANCELLED')))
            ORDER BY p.createdAt DESC
            """)
    Page<PrescriptionLink> search(
            @Param("prescriptionId") String prescriptionId,
            @Param("patientId") String patientId,
            @Param("physicianId") String physicianId,
            @Param("departmentId") String departmentId,
            @Param("stage") String stage,
            Pageable pageable);
}
