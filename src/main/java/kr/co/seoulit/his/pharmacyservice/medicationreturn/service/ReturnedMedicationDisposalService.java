package kr.co.seoulit.his.pharmacyservice.medicationreturn.service;

import kr.co.seoulit.his.pharmacyservice.common.BusinessException;
import kr.co.seoulit.his.pharmacyservice.common.ErrorCode;
import kr.co.seoulit.his.pharmacyservice.disposal.entity.MedicationDisposal;
import kr.co.seoulit.his.pharmacyservice.disposal.entity.MedicationDisposalItem;
import kr.co.seoulit.his.pharmacyservice.disposal.repository.MedicationDisposalItemRepository;
import kr.co.seoulit.his.pharmacyservice.disposal.repository.MedicationDisposalRepository;
import kr.co.seoulit.his.pharmacyservice.inventory.service.StockMovementService;
import kr.co.seoulit.his.pharmacyservice.medicationreturn.dto.ReturnedMedicationDisposalCreateRequest;
import kr.co.seoulit.his.pharmacyservice.medicationreturn.entity.MedicationReturnItem;
import kr.co.seoulit.his.pharmacyservice.medicationreturn.entity.ReturnedMedicationDisposal;
import kr.co.seoulit.his.pharmacyservice.medicationreturn.repository.MedicationReturnItemRepository;
import kr.co.seoulit.his.pharmacyservice.medicationreturn.repository.ReturnedMedicationDisposalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/**
 * 반납약품폐기(HL2-23). 반납(MedicationReturnItem)된 수량 중 재사용하지 않고 버릴 부분을
 * 실제 MEDICATION_DISPOSAL(헤더/상세)로 기록하고, 반납 때 복구했던 재고를 다시 차감한다.
 */
@Service
public class ReturnedMedicationDisposalService {

    private static final String SOURCE_FORM_TYPE_DISPOSAL = "DISPOSAL";
    private static final String DISPOSED_BY_PLACEHOLDER = "SYSTEM";
    private static final int REASON_MAX_BYTES = 20;

    private final MedicationReturnItemRepository medicationReturnItemRepository;
    private final MedicationDisposalRepository medicationDisposalRepository;
    private final MedicationDisposalItemRepository medicationDisposalItemRepository;
    private final ReturnedMedicationDisposalRepository returnedMedicationDisposalRepository;
    private final StockMovementService stockMovementService;

    public ReturnedMedicationDisposalService(MedicationReturnItemRepository medicationReturnItemRepository,
                                              MedicationDisposalRepository medicationDisposalRepository,
                                              MedicationDisposalItemRepository medicationDisposalItemRepository,
                                              ReturnedMedicationDisposalRepository returnedMedicationDisposalRepository,
                                              StockMovementService stockMovementService) {
        this.medicationReturnItemRepository = medicationReturnItemRepository;
        this.medicationDisposalRepository = medicationDisposalRepository;
        this.medicationDisposalItemRepository = medicationDisposalItemRepository;
        this.returnedMedicationDisposalRepository = returnedMedicationDisposalRepository;
        this.stockMovementService = stockMovementService;
    }

    @Transactional
    public ReturnedMedicationDisposal create(String medicationReturnItemId, ReturnedMedicationDisposalCreateRequest request) {
        if (request.reason().getBytes(StandardCharsets.UTF_8).length > REASON_MAX_BYTES) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        MedicationReturnItem returnItem = medicationReturnItemRepository.findById(medicationReturnItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICATION_RETURN_ITEM_NOT_FOUND));

        BigDecimal alreadyDisposed = returnItem.getDisposalQty();
        BigDecimal newTotal = alreadyDisposed.add(request.disposalQty());
        if (newTotal.compareTo(returnItem.getReturnQty()) > 0) {
            throw new BusinessException(ErrorCode.RETURN_QTY_EXCEEDS_DISPENSED);
        }

        MedicationDisposal disposal = medicationDisposalRepository.save(
                new MedicationDisposal(LocalDate.now(), DISPOSED_BY_PLACEHOLDER, request.reason()));

        medicationDisposalItemRepository.save(new MedicationDisposalItem(
                disposal, returnItem.getDispensingItem().getMedicationLot(), request.disposalQty()));

        // 반납 때 원래 로트로 복구해둔 재고를, 폐기 결정이 난 수량만큼 다시 차감한다.
        stockMovementService.decreaseLot(
                returnItem.getDispensingItem().getMedicationLot(), request.disposalQty(), StockMovementService.STOCK_TX_TYPE_DISPOSAL,
                disposal.getMedicationDisposalId(), SOURCE_FORM_TYPE_DISPOSAL, DISPOSED_BY_PLACEHOLDER);

        returnItem.addDisposalQty(request.disposalQty());

        return returnedMedicationDisposalRepository.save(
                new ReturnedMedicationDisposal(returnItem, disposal, request.disposalQty(), request.reason()));
    }
}
