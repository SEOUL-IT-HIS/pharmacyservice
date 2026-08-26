package kr.co.seoulit.his.pharmacyservice.prescription.service;

import kr.co.seoulit.his.pharmacyservice.common.BusinessException;
import kr.co.seoulit.his.pharmacyservice.common.ErrorCode;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionDetailResponse;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionItemResponse;
import kr.co.seoulit.his.pharmacyservice.prescription.dto.PrescriptionListResponse;
import kr.co.seoulit.his.pharmacyservice.prescription.entity.PrescriptionLink;
import kr.co.seoulit.his.pharmacyservice.prescription.repository.PrescriptionItemLinkRepository;
import kr.co.seoulit.his.pharmacyservice.prescription.repository.PrescriptionLinkRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PrescriptionService {

    private final PrescriptionLinkRepository prescriptionLinkRepository;
    private final PrescriptionItemLinkRepository prescriptionItemLinkRepository;

    public PrescriptionService(PrescriptionLinkRepository prescriptionLinkRepository,
                                PrescriptionItemLinkRepository prescriptionItemLinkRepository) {
        this.prescriptionLinkRepository = prescriptionLinkRepository;
        this.prescriptionItemLinkRepository = prescriptionItemLinkRepository;
    }

    public Page<PrescriptionListResponse> search(String prescriptionId, String patientId, String physicianId,
                                                  String departmentId, Pageable pageable) {
        return prescriptionLinkRepository
                .search(prescriptionId, patientId, physicianId, departmentId, pageable)
                .map(PrescriptionListResponse::from);
    }

    public PrescriptionDetailResponse getDetail(String prescriptionLinkId) {
        PrescriptionLink link = prescriptionLinkRepository.findById(prescriptionLinkId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRESCRIPTION_NOT_FOUND));

        List<PrescriptionItemResponse> items = prescriptionItemLinkRepository
                .findByPrescriptionLink_PrescriptionLinkId(prescriptionLinkId)
                .stream()
                .map(PrescriptionItemResponse::from)
                .toList();

        return PrescriptionDetailResponse.from(link, items);
    }
}
