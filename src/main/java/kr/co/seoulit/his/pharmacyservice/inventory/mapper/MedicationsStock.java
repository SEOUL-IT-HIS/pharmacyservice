package kr.co.seoulit.his.pharmacyservice.inventory.mapper;

import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MedicationsStock {

    void medicationsStock(Map<String, Object> params);
}
