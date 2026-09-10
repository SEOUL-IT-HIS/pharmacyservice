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
            ORDER BY p.createdAt DESC
            """)
    Page<PrescriptionLink> search(
            @Param("prescriptionId") String prescriptionId,
            @Param("patientId") String patientId,
            @Param("physicianId") String physicianId,
            @Param("departmentId") String departmentId,
            Pageable pageable);
}
