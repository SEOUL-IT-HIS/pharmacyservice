package kr.co.seoulit.his.pharmacyservice.receipt.dto;

import java.util.List;

public record ReceiptCreateResponse(
        String medicationReceiptId,
        List<ReceiptItemResult> items
) {
}
