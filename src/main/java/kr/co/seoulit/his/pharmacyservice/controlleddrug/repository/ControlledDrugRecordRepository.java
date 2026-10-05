package kr.co.seoulit.his.pharmacyservice.controlleddrug.repository;

import kr.co.seoulit.his.pharmacyservice.controlleddrug.entity.ControlledDrugRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ControlledDrugRecordRepository extends JpaRepository<ControlledDrugRecord, String> {

    /** 특수약품 기록 조회(전체) / 마약류 입고·출고 조회(controlledTxCd로 필터) 공용 */
    List<ControlledDrugRecord> findByControlledTxCd(String controlledTxCd);
}
