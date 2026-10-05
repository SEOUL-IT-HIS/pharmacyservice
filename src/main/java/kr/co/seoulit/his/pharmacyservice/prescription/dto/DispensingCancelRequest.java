package kr.co.seoulit.his.pharmacyservice.prescription.dto;

import jakarta.validation.constraints.NotBlank;

public record DispensingCancelRequest(
        @NotBlank String reason
) {
}
