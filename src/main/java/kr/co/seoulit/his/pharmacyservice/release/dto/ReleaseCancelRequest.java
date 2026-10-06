package kr.co.seoulit.his.pharmacyservice.release.dto;

import jakarta.validation.constraints.NotBlank;

public record ReleaseCancelRequest(
        @NotBlank String reason,
        @NotBlank String actorId
) {
}
