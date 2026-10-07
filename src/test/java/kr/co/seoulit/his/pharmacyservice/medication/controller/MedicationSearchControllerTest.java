package kr.co.seoulit.his.pharmacyservice.medication.controller;

import kr.co.seoulit.his.pharmacyservice.medication.dto.MedicationDto;
import kr.co.seoulit.his.pharmacyservice.medication.service.MedicationService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MedicationSearchController.class)
class MedicationSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MedicationService medicationService;

    private MedicationDto newDto(String name, String ediCode) {
        MedicationDto dto = new MedicationDto();
        dto.setMedicationId(1L);
        dto.setMedicationName(name);
        dto.setEdiCode(ediCode);
        return dto;
    }

    @Test
    void searchPage_returnsPage_withoutName() throws Exception {
        Page<MedicationDto> page = new PageImpl<>(List.of(newDto("타이레놀정500mg", "EDI-TYLENOL-500")), PageRequest.of(0, 20), 1);
        when(medicationService.searchPage(eq(""), eq(false), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/pharmacy/medications/page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].ediCode").value("EDI-TYLENOL-500"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void searchPage_passesNameAndEdiOnly() throws Exception {
        when(medicationService.searchPage(any(), anyBoolean(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/api/pharmacy/medications/page")
                        .param("name", "타이레놀")
                        .param("ediCodeOnly", "true"))
                .andExpect(status().isOk());

        verify(medicationService).searchPage(eq("타이레놀"), eq(true), any(Pageable.class));
    }

    @Test
    void searchPage_capsSizeAt100() throws Exception {
        when(medicationService.searchPage(any(), anyBoolean(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 100), 0));

        mockMvc.perform(get("/api/pharmacy/medications/page").param("size", "5000").param("page", "2"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(medicationService).searchPage(eq(""), eq(false), captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(100);
        assertThat(captor.getValue().getPageNumber()).isEqualTo(2);
    }
}
