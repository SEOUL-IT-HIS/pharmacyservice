package kr.co.seoulit.his.pharmacyservice.prescription.controller;

import kr.co.seoulit.his.pharmacyservice.common.exception.BusinessException;
import kr.co.seoulit.his.pharmacyservice.common.exception.ErrorCode;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionDetailResponse;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionListResponse;
import kr.co.seoulit.his.pharmacyservice.prescription.service.PrescriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PrescriptionController.class)
class PrescriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PrescriptionService prescriptionService;

    @Test
    void search_returns200_withList() throws Exception {
        PrescriptionListResponse response = new PrescriptionListResponse(
                "LINK-001", "PRESCRIPTION-001", "PATIENT-001", "PHYSICIAN-001", "DEPARTMENT-001",
                LocalDateTime.of(2026, 7, 16, 9, 0));
        Page<PrescriptionListResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);
        when(prescriptionService.search(any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/pharmacy/prescriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].prescriptionLinkId").value("LINK-001"));
    }

    @Test
    void getDetail_returns200_withItems() throws Exception {
        PrescriptionDetailResponse response = new PrescriptionDetailResponse(
                "LINK-001", "PRESCRIPTION-001", "PATIENT-001", "PHYSICIAN-001", "DEPARTMENT-001",
                LocalDateTime.of(2026, 7, 16, 9, 0), List.of());
        when(prescriptionService.getDetail("LINK-001")).thenReturn(response);

        mockMvc.perform(get("/api/pharmacy/prescriptions/LINK-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.prescriptionId").value("PRESCRIPTION-001"));
    }

    @Test
    void getDetail_returns404_whenNotFound() throws Exception {
        when(prescriptionService.getDetail("missing-id"))
                .thenThrow(new BusinessException(ErrorCode.PRESCRIPTION_NOT_FOUND));

        mockMvc.perform(get("/api/pharmacy/prescriptions/missing-id"))
                .andExpect(status().isNotFound());
    }
}
