package kr.co.seoulit.his.pharmacyservice.inventory.controller;

import kr.co.seoulit.his.pharmacyservice.common.exception.BusinessException;
import kr.co.seoulit.his.pharmacyservice.common.exception.ErrorCode;
import kr.co.seoulit.his.pharmacyservice.inventory.dto.InventoryResponse;
import kr.co.seoulit.his.pharmacyservice.inventory.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventoryService inventoryService;

    @Test
    void search_returns200_withList() throws Exception {
        InventoryResponse response = new InventoryResponse(
                "STOCK-001", "LOT-001", "MEDICATION-001", "LOT-001",
                LocalDate.of(2027, 12, 31), LocalDate.of(2026, 6, 1), "EA",
                "LOCATION-001", BigDecimal.TEN, null);
        Page<InventoryResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);
        when(inventoryService.search(any(), any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/pharmacy/inventories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].medicationStockId").value("STOCK-001"));
    }

    @Test
    void getDetail_returns404_whenNotFound() throws Exception {
        when(inventoryService.getDetail("missing-id"))
                .thenThrow(new BusinessException(ErrorCode.MEDICATION_STOCK_NOT_FOUND));

        mockMvc.perform(get("/api/pharmacy/inventories/missing-id"))
                .andExpect(status().isNotFound());
    }
}
