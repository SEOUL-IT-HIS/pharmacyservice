package kr.co.seoulit.his.pharmacyservice.medication.repository;

import java.util.List;
import java.util.Optional;
import kr.co.seoulit.his.pharmacyservice.medication.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicationRepository extends JpaRepository<Medication, Long> {

    Optional<Medication> findByItemSeq(String itemSeq);

    // EDI_CODE 컬럼엔 콤마로 이어진 여러 코드가 들어올 수 있어 부분일치로 조회한다.
    // (예: "645700210,645700220" 저장 시 "645700210" 단독 검색도 매칭되어야 함)
    @Query("SELECT m FROM Medication m WHERE m.ediCode LIKE CONCAT('%', :ediCode, '%')")
    Optional<Medication> findByEdiCodeContaining(@Param("ediCode") String ediCode);

    // 외래 처방전 작성 화면의 약품 검색(자동완성)용 — 이름 일부만 입력해도 매칭, 상위 20건만 반환
    List<Medication> findTop20ByMedicationNameContainingIgnoreCaseOrderByMedicationNameAsc(String medicationName);
}
