package kr.co.seoulit.his.pharmacyservice.medication.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MedicationStockInRequest {

    private Long medicationId;
    private Integer quantity;
}
