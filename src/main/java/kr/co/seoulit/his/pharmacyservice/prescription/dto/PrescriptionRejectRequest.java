package kr.co.seoulit.his.pharmacyservice.prescription.dto;

import jakarta.validation.constraints.NotBlank;

public record PrescriptionRejectRequest(
        @NotBlank String reason
) {
}
