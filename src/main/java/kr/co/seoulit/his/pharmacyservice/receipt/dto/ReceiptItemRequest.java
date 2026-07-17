package kr.co.seoulit.his.pharmacyservice.receipt.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReceiptItemRequest(
        @NotBlank String medicationId,
        @NotBlank String lotNo,
        @NotNull LocalDate expirationDt,
        LocalDate manufactureDt,
        @NotBlank String unitCd,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal receiptQty,
        @DecimalMin(value = "0.0") BigDecimal unitPrice
) {
}
