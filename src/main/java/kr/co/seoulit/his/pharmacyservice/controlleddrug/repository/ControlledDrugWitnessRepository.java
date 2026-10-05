package kr.co.seoulit.his.pharmacyservice.controlleddrug.repository;

import kr.co.seoulit.his.pharmacyservice.controlleddrug.entity.ControlledDrugWitness;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ControlledDrugWitnessRepository extends JpaRepository<ControlledDrugWitness, String> {

    List<ControlledDrugWitness> findByControlledDrugRecord_ControlledDrugRecordId(String controlledDrugRecordId);
}
