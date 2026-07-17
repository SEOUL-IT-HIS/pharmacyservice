package kr.co.seoulit.his.pharmacyservice.receipt.controller;

import kr.co.seoulit.his.pharmacyservice.common.exception.BusinessException;
import kr.co.seoulit.his.pharmacyservice.common.exception.ErrorCode;
import kr.co.seoulit.his.pharmacyservice.receipt.dto.ReceiptCreateResponse;
import kr.co.seoulit.his.pharmacyservice.receipt.dto.ReceiptItemResult;
import kr.co.seoulit.his.pharmacyservice.receipt.service.ReceiptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReceiptController.class)
class ReceiptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReceiptService receiptService;

    private static final String VALID_REQUEST = """
            {
              "supplierId": "SUPPLIER-001",
              "storageLocationId": "LOCATION-001",
              "receiptDt": "2026-07-16",
              "receivedById": "STAFF-001",
              "items": [
                {
                  "medicationId": "MEDICATION-001",
                  "lotNo": "LOT-2026-001",
                  "expirationDt": "2027-12-31",
                  "manufactureDt": "2026-06-01",
                  "unitCd": "EA",
                  "receiptQty": 100,
                  "unitPrice": 500
                }
              ]
            }
            """;

    @Test
    void createReceipt_returns201_whenValid() throws Exception {
        when(receiptService.createReceipt(any())).thenReturn(new ReceiptCreateResponse(
                "RECEIPT-001",
                List.of(new ReceiptItemResult("ITEM-001", "LOT-001", "STOCK-001", BigDecimal.valueOf(100)))));

        mockMvc.perform(post("/api/pharmacy/receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isCreated());
    }

    @Test
    void createReceipt_returns400_whenItemsEmpty() throws Exception {
        String body = """
                {
                  "supplierId": "SUPPLIER-001",
                  "storageLocationId": "LOCATION-001",
                  "receiptDt": "2026-07-16",
                  "receivedById": "STAFF-001",
                  "items": []
                }
                """;

        mockMvc.perform(post("/api/pharmacy/receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReceipt_returns400_whenHeaderFieldMissing() throws Exception {
        String body = """
                {
                  "storageLocationId": "LOCATION-001",
                  "receiptDt": "2026-07-16",
                  "receivedById": "STAFF-001",
                  "items": [
                    {
                      "medicationId": "MEDICATION-001",
                      "lotNo": "LOT-2026-001",
                      "expirationDt": "2027-12-31",
                      "unitCd": "EA",
                      "receiptQty": 100
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/pharmacy/receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReceipt_returns400_whenReceiptQtyNotPositive() throws Exception {
        String body = """
                {
                  "supplierId": "SUPPLIER-001",
                  "storageLocationId": "LOCATION-001",
                  "receiptDt": "2026-07-16",
                  "receivedById": "STAFF-001",
                  "items": [
                    {
                      "medicationId": "MEDICATION-001",
                      "lotNo": "LOT-2026-001",
                      "expirationDt": "2027-12-31",
                      "unitCd": "EA",
                      "receiptQty": 0
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/pharmacy/receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReceipt_returns400_whenDuplicateItemInSameRequest() throws Exception {
        when(receiptService.createReceipt(any()))
                .thenThrow(new BusinessException(ErrorCode.DUPLICATE_RECEIPT_ITEM));

        mockMvc.perform(post("/api/pharmacy/receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isBadRequest());
    }
}
