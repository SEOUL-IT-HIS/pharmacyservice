package kr.co.seoulit.his.pharmacyservice.disposal.service;

import kr.co.seoulit.his.pharmacyservice.common.BusinessException;
import kr.co.seoulit.his.pharmacyservice.common.ErrorCode;
import kr.co.seoulit.his.pharmacyservice.disposal.dto.DisposalCreateRequest;
import kr.co.seoulit.his.pharmacyservice.disposal.entity.MedicationDisposal;
import kr.co.seoulit.his.pharmacyservice.disposal.entity.MedicationDisposalItem;
import kr.co.seoulit.his.pharmacyservice.disposal.repository.MedicationDisposalItemRepository;
import kr.co.seoulit.his.pharmacyservice.disposal.repository.MedicationDisposalRepository;
import kr.co.seoulit.his.pharmacyservice.inventory.service.StockMovementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

/**
 * 약품 폐기(HL2-10 등록).
 * MEDICATION_DISPOSAL(헤더) + MEDICATION_DISPOSAL_ITEM(상세)에 정식으로 기록하고, 재고 차감은
 * 유효기간이 빠른 로트(FEFO)부터 차감하고, 한 로트로 모자라면 여러 로트로 나눠 차감한다.
 */
@Service
public class DisposalService {

    private static final String SOURCE_FORM_TYPE_DISPOSAL = "DISPOSAL";
    /** DISPOSAL_REASON_CD 컬럼(200바이트)에 들어가는 사유만 저장하고 넘으면 DISPOSAL_REASON_TOO_LONG으로 막는다 */
    private static final int DISPOSAL_REASON_MAX_BYTES = 200;

    private final MedicationDisposalRepository medicationDisposalRepository;
    private final MedicationDisposalItemRepository medicationDisposalItemRepository;
    private final StockMovementService stockMovementService;

    public DisposalService(MedicationDisposalRepository medicationDisposalRepository,
                            MedicationDisposalItemRepository medicationDisposalItemRepository,
                            StockMovementService stockMovementService) {
        this.medicationDisposalRepository = medicationDisposalRepository;
        this.medicationDisposalItemRepository = medicationDisposalItemRepository;
        this.stockMovementService = stockMovementService;
    }

    // 마약류 폐기(controlleddrug 패키지)가 이 메서드를 그대로 재사용하면서, 방금 생긴
    // INVENTORY_MOVEMENT를 찾아 CONTROLLED_DRUG_RECORD를 남겨야 해서 헤더를 반환한다.
    @Transactional
    public MedicationDisposal create(DisposalCreateRequest request, String disposedById) {
        if (request.reason().getBytes(StandardCharsets.UTF_8).length > DISPOSAL_REASON_MAX_BYTES) {
            throw new BusinessException(ErrorCode.DISPOSAL_REASON_TOO_LONG);
        }

        MedicationDisposal disposal = medicationDisposalRepository.save(
                new MedicationDisposal(LocalDate.now(), disposedById, request.reason()));

        List<StockMovementService.Result> results = stockMovementService.decreaseFefo(
                request.medicationId(), request.quantity(), StockMovementService.STOCK_TX_TYPE_DISPOSAL,
                disposal.getMedicationDisposalId(), SOURCE_FORM_TYPE_DISPOSAL, disposedById);
        for (StockMovementService.LotQty lotQty : StockMovementService.groupByLot(results)) {
            medicationDisposalItemRepository.save(new MedicationDisposalItem(disposal, lotQty.lot(), lotQty.qty()));
        }

        return disposal;
    }
}
