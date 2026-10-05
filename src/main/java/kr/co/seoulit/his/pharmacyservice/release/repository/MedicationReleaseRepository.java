package kr.co.seoulit.his.pharmacyservice.release.repository;

import kr.co.seoulit.his.pharmacyservice.release.entity.MedicationRelease;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicationReleaseRepository extends JpaRepository<MedicationRelease, String> {

    Optional<MedicationRelease> findByDispensing_DispensingIdAndReleaseStatusCd(String dispensingId, String releaseStatusCd);

    // DB에 UQ_MEDICATION_RELEASE_1(DISPENSING_ID) 유니크 제약이 있다 — 취소됐더라도 같은
    // Dispensing으로 또 불출 레코드를 만들 수 없다. 생성 전에 상태 무관하게 존재 여부를 확인한다.
    boolean existsByDispensing_DispensingId(String dispensingId);

    // 처방전 상세 화면에서 보여줄 불출 정보 — 취소된 것도 "다시 불출할 수 없다"는 걸 알려줘야
    // 하므로 상태 무관하게 조회한다.
    Optional<MedicationRelease> findByDispensing_DispensingId(String dispensingId);
}
