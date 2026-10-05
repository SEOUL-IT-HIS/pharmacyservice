package kr.co.seoulit.his.pharmacyservice.controlleddrug.service;

import kr.co.seoulit.his.pharmacyservice.common.BusinessException;
import kr.co.seoulit.his.pharmacyservice.common.ErrorCode;
import kr.co.seoulit.his.pharmacyservice.controlleddrug.dto.ControlledDrugDisposalRequest;
import kr.co.seoulit.his.pharmacyservice.controlleddrug.dto.ControlledDrugIssuanceRequest;
import kr.co.seoulit.his.pharmacyservice.controlleddrug.dto.ControlledDrugReceiptRequest;
import kr.co.seoulit.his.pharmacyservice.controlleddrug.dto.ControlledDrugRecordResponse;
import kr.co.seoulit.his.pharmacyservice.controlleddrug.entity.ControlledDrugRecord;
import kr.co.seoulit.his.pharmacyservice.controlleddrug.entity.ControlledDrugWitness;
import kr.co.seoulit.his.pharmacyservice.controlleddrug.repository.ControlledDrugRecordRepository;
import kr.co.seoulit.his.pharmacyservice.controlleddrug.repository.ControlledDrugWitnessRepository;
import kr.co.seoulit.his.pharmacyservice.disposal.entity.MedicationDisposal;
import kr.co.seoulit.his.pharmacyservice.disposal.service.DisposalService;
import kr.co.seoulit.his.pharmacyservice.inventory.service.StockMovementService;
import kr.co.seoulit.his.pharmacyservice.issuance.entity.MedicationIssue;
import kr.co.seoulit.his.pharmacyservice.issuance.service.IssuanceService;
import kr.co.seoulit.his.pharmacyservice.receipt.dto.ReceiptCreateResponse;
import kr.co.seoulit.his.pharmacyservice.receipt.entity.InventoryMovement;
import kr.co.seoulit.his.pharmacyservice.receipt.repository.InventoryMovementRepository;
import kr.co.seoulit.his.pharmacyservice.receipt.service.ReceiptService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 마약류 입고/출고/폐기(특수약품관리). 재고 처리 자체는 기존 ReceiptService/IssuanceService/
 * DisposalService를 그대로 호출해서 위임하고, 이 서비스는 그 결과로 생긴 재고 이력마다
 * CONTROLLED_DRUG_RECORD(처리자) + CONTROLLED_DRUG_WITNESS(입회자)만 얹는다.
 * 입회자 없이는 마약류 거래를 기록할 수 없다(PHM019) — 법적으로 2인 이상 확인이 필요하기 때문.
 */
@Service
public class ControlledDrugRecordService {

    private final ReceiptService receiptService;
    private final IssuanceService issuanceService;
    private final DisposalService disposalService;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final ControlledDrugRecordRepository controlledDrugRecordRepository;
    private final ControlledDrugWitnessRepository controlledDrugWitnessRepository;

    public ControlledDrugRecordService(ReceiptService receiptService,
                                        IssuanceService issuanceService,
                                        DisposalService disposalService,
                                        InventoryMovementRepository inventoryMovementRepository,
                                        ControlledDrugRecordRepository controlledDrugRecordRepository,
                                        ControlledDrugWitnessRepository controlledDrugWitnessRepository) {
        this.receiptService = receiptService;
        this.issuanceService = issuanceService;
        this.disposalService = disposalService;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.controlledDrugRecordRepository = controlledDrugRecordRepository;
        this.controlledDrugWitnessRepository = controlledDrugWitnessRepository;
    }

    @Transactional
    public List<ControlledDrugRecordResponse> recordReceipt(ControlledDrugReceiptRequest request) {
        ReceiptCreateResponse response = receiptService.createReceipt(request.receipt());
        List<InventoryMovement> movements = inventoryMovementRepository.findBySourceFormId(response.medicationReceiptId());
        return recordAll(movements, request.staffId(), StockMovementService.STOCK_TX_TYPE_RECEIPT, request.witnessStaffIds());
    }

    @Transactional
    public List<ControlledDrugRecordResponse> recordIssuance(ControlledDrugIssuanceRequest request) {
        MedicationIssue issue = issuanceService.create(request.issuance());
        List<InventoryMovement> movements = inventoryMovementRepository.findBySourceFormId(issue.getMedicationIssueId());
        return recordAll(movements, request.staffId(), StockMovementService.STOCK_TX_TYPE_ISSUANCE, request.witnessStaffIds());
    }

    @Transactional
    public List<ControlledDrugRecordResponse> recordDisposal(ControlledDrugDisposalRequest request) {
        MedicationDisposal disposal = disposalService.create(request.disposal());
        List<InventoryMovement> movements = inventoryMovementRepository.findBySourceFormId(disposal.getMedicationDisposalId());
        return recordAll(movements, request.staffId(), StockMovementService.STOCK_TX_TYPE_DISPOSAL, request.witnessStaffIds());
    }

    private List<ControlledDrugRecordResponse> recordAll(List<InventoryMovement> movements, String staffId,
                                                           String controlledTxCd, List<String> witnessStaffIds) {
        if (witnessStaffIds == null || witnessStaffIds.isEmpty()) {
            throw new BusinessException(ErrorCode.CONTROLLED_DRUG_WITNESS_REQUIRED);
        }

        LocalDateTime witnessedAt = LocalDateTime.now();
        return movements.stream()
                .map(movement -> {
                    ControlledDrugRecord record = controlledDrugRecordRepository.save(
                            new ControlledDrugRecord(movement, staffId, controlledTxCd));
                    for (String witnessStaffId : witnessStaffIds) {
                        controlledDrugWitnessRepository.save(new ControlledDrugWitness(
                                record, witnessStaffId, ControlledDrugWitness.ROLE_WITNESS, witnessedAt));
                    }
                    return ControlledDrugRecordResponse.from(record, witnessStaffIds);
                })
                .toList();
    }

    /**
     * 특수약품 기록 조회(전체) / 마약류 입고·출고 조회(controlledTxCd로 필터) 공용.
     * InventoryMovement/입회자 목록까지 여기서 전부 DTO로 바꿔서 반환한다 — 트랜잭션 밖(컨트롤러)에서
     * record.getInventoryMovement()를 건드리면 지연로딩 세션이 이미 닫혀 있어 예외가 난다.
     */
    @Transactional(readOnly = true)
    public List<ControlledDrugRecordResponse> findRecords(String controlledTxCd) {
        List<ControlledDrugRecord> records = controlledTxCd == null
                ? controlledDrugRecordRepository.findAll()
                : controlledDrugRecordRepository.findByControlledTxCd(controlledTxCd);

        return records.stream()
                .map(record -> ControlledDrugRecordResponse.from(record, findWitnessStaffIds(record.getControlledDrugRecordId())))
                .toList();
    }

    private List<String> findWitnessStaffIds(String controlledDrugRecordId) {
        return controlledDrugWitnessRepository.findByControlledDrugRecord_ControlledDrugRecordId(controlledDrugRecordId)
                .stream()
                .map(ControlledDrugWitness::getWitnessStaffId)
                .toList();
    }
}
