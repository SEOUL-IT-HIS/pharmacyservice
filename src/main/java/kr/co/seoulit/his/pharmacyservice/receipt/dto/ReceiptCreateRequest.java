package kr.co.seoulit.his.pharmacyservice.receipt.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record ReceiptCreateRequest(
        @NotBlank String supplierId,
        @NotBlank String storageLocationId,
        @NotNull LocalDate receiptDt,
        @NotBlank String receivedById,
        @NotEmpty @Valid List<ReceiptItemRequest> items
) {
}
