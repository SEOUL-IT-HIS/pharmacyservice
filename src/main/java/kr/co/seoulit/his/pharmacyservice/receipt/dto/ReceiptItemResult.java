package kr.co.seoulit.his.pharmacyservice.receipt.dto;

import java.math.BigDecimal;

public record ReceiptItemResult(
        String medicationReceiptItemId,
        String medicationLotId,
        String medicationStockId,
        BigDecimal currentQty
) {
}
