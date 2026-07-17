package kr.co.seoulit.his.pharmacyservice.prescription.service;

import kr.co.seoulit.his.pharmacyservice.common.exception.BusinessException;
import kr.co.seoulit.his.pharmacyservice.common.exception.ErrorCode;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionDetailResponse;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionListResponse;
import kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionLink;
import kr.co.seoulit.his.pharmacyservice.prescription.repository.PrescriptionItemLinkRepository;
import kr.co.seoulit.his.pharmacyservice.prescription.repository.PrescriptionLinkRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceTest {

    @Mock
    private PrescriptionLinkRepository prescriptionLinkRepository;
    @Mock
    private PrescriptionItemLinkRepository prescriptionItemLinkRepository;

    @InjectMocks
    private PrescriptionService prescriptionService;

    private PrescriptionLink newPrescriptionLink() {
        PrescriptionLink link = new PrescriptionLink();
        ReflectionTestUtils.setField(link, "prescriptionLinkId", UUID.randomUUID().toString());
        ReflectionTestUtils.setField(link, "prescriptionId", "PRESCRIPTION-001");
        ReflectionTestUtils.setField(link, "patientId", "PATIENT-001");
        ReflectionTestUtils.setField(link, "physicianId", "PHYSICIAN-001");
        ReflectionTestUtils.setField(link, "departmentId", "DEPARTMENT-001");
        return link;
    }

    @Test
    void search_returnsMappedPage() {
        PrescriptionLink link = newPrescriptionLink();
        Pageable pageable = PageRequest.of(0, 20);
        when(prescriptionLinkRepository.search(eq("PRESCRIPTION-001"), any(), any(), any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(link), pageable, 1));

        Page<PrescriptionListResponse> result = prescriptionService.search(
                "PRESCRIPTION-001", null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).prescriptionId()).isEqualTo("PRESCRIPTION-001");
    }

    @Test
    void getDetail_returnsResponseWithItems_whenExists() {
        PrescriptionLink link = newPrescriptionLink();
        when(prescriptionLinkRepository.findById(link.getPrescriptionLinkId())).thenReturn(Optional.of(link));
        when(prescriptionItemLinkRepository.findByPrescriptionLink_PrescriptionLinkId(link.getPrescriptionLinkId()))
                .thenReturn(List.of());

        PrescriptionDetailResponse response = prescriptionService.getDetail(link.getPrescriptionLinkId());

        assertThat(response.prescriptionLinkId()).isEqualTo(link.getPrescriptionLinkId());
        assertThat(response.items()).isEmpty();
    }

    @Test
    void getDetail_throwsNotFound_whenMissing() {
        when(prescriptionLinkRepository.findById("missing-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> prescriptionService.getDetail("missing-id"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRESCRIPTION_NOT_FOUND);
    }
}
