package kr.co.seoulit.his.pharmacyservice.medication.repository;

import java.util.List;
import java.util.Optional;
import kr.co.seoulit.his.pharmacyservice.medication.entity.Medication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicationRepository extends JpaRepository<Medication, Long> {

    Optional<Medication> findByItemSeq(String itemSeq);

    // 타 서비스(응급 등) 약품 선택 목록용. name이 빈 문자열이면 전체, ediOnly면 EDI 코드가 있는 약품만(코드가 없으면 처방 접수가 안 된다).
    // ⚠ Oracle은 빈 문자열('')을 NULL로 저장하므로 "<> ''" 비교는 항상 거짓이 된다 — IS NOT NULL만 쓴다.
    @Query("""
            SELECT m FROM Medication m
            WHERE LOWER(m.medicationName) LIKE LOWER(CONCAT('%', :name, '%'))
              AND (:ediOnly = false OR m.ediCode IS NOT NULL)
            ORDER BY m.medicationName ASC, m.medicationId ASC
            """)
    Page<Medication> searchPage(@Param("name") String name, @Param("ediOnly") boolean ediOnly, Pageable pageable);

    // EDI_CODE 컬럼엔 콤마로 이어진 여러 코드가 들어올 수 있어 부분일치로 조회한다.
    // (예: "645700210,645700220" 저장 시 "645700210" 단독 검색도 매칭되어야 함)
    @Query("SELECT m FROM Medication m WHERE m.ediCode LIKE CONCAT('%', :ediCode, '%')")
    Optional<Medication> findByEdiCodeContaining(@Param("ediCode") String ediCode);

    // 외래 처방전 작성 화면의 약품 검색(자동완성)용 — 이름 일부만 입력해도 매칭, 상위 20건만 반환
    List<Medication> findTop20ByMedicationNameContainingIgnoreCaseOrderByMedicationNameAsc(String medicationName);
}
