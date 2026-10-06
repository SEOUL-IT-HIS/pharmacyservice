package kr.co.seoulit.his.pharmacyservice.storagelocation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StorageLocationRegisterRequest {

    @NotBlank
    private String locationName;
}
