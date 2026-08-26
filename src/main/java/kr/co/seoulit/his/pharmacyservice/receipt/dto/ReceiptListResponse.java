package kr.co.seoulit.his.pharmacyservice.receipt.dto;

import kr.co.seoulit.his.pharmacyservice.receipt.entity.MedicationReceipt;
import kr.co.seoulit.his.pharmacyservice.receipt.entity.MedicationReceiptItem;
import kr.co.seoulit.his.pharmacyservice.inventory.entity.MedicationLot;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 입고 조회(HL2-7) 화면용 — 입고 항목을 약품 단위로 평탄화해서 보여준다.
 */
public record ReceiptListResponse(
        String medicationId,
        String medicationName,
        String lotNo,
        LocalDate expirationDt,
        LocalDate receiptDt,
        String storageLocationId,
        String supplierId,
        BigDecimal quantity,
        BigDecimal unitPrice
) {

    public static ReceiptListResponse from(MedicationReceiptItem item, String medicationName) {
        MedicationLot lot = item.getMedicationLot();
        MedicationReceipt receipt = item.getMedicationReceipt();
        return new ReceiptListResponse(
                lot.getMedicationId(),
                medicationName,
                lot.getLotNo(),
                lot.getExpirationDt(),
                receipt.getReceiptDt(),
                receipt.getStorageLocationId(),
                receipt.getSupplierId(),
                item.getReceiptQty(),
                item.getUnitPrice()
        );
    }
}
