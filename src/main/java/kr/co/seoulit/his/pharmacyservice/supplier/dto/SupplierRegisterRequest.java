package kr.co.seoulit.his.pharmacyservice.supplier.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupplierRegisterRequest {

    @NotBlank
    private String supplierName;
    private String contactPhone;
}
