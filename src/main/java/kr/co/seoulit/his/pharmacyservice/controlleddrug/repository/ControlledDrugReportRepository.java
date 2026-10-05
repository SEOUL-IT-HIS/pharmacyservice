package kr.co.seoulit.his.pharmacyservice.controlleddrug.repository;

import kr.co.seoulit.his.pharmacyservice.controlleddrug.entity.ControlledDrugReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ControlledDrugReportRepository extends JpaRepository<ControlledDrugReport, String> {
}
