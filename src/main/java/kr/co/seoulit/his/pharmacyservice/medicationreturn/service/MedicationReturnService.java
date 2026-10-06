package kr.co.seoulit.his.pharmacyservice.medicationreturn.service;

import kr.co.seoulit.his.pharmacyservice.common.BusinessException;
import kr.co.seoulit.his.pharmacyservice.common.ErrorCode;
import kr.co.seoulit.his.pharmacyservice.inventory.service.StockMovementService;
import kr.co.seoulit.his.pharmacyservice.medicationreturn.dto.MedicationReturnCreateRequest;
import kr.co.seoulit.his.pharmacyservice.medicationreturn.entity.MedicationReturn;
import kr.co.seoulit.his.pharmacyservice.medicationreturn.entity.MedicationReturnItem;
import kr.co.seoulit.his.pharmacyservice.medicationreturn.repository.MedicationReturnItemRepository;
import kr.co.seoulit.his.pharmacyservice.medicationreturn.repository.MedicationReturnRepository;
import kr.co.seoulit.his.pharmacyservice.prescription.entity.DispensingItem;
import kr.co.seoulit.his.pharmacyservice.prescription.repository.DispensingItemRepository;
import kr.co.seoulit.his.pharmacyservice.release.entity.MedicationRelease;
import kr.co.seoulit.his.pharmacyservice.release.repository.MedicationReleaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/**
 * 약품 반납(HL2-22). 불출(MedicationRelease)된 약을 환자/병동이 다시 가져온 것을 기록하고,
 * 반납된 수량만큼 원래 빠져나간 로트로 재고를 복구한다(실제로 폐기할 부분은 반납약품폐기
 * 단계에서 다시 차감한다 — {@link ReturnedMedicationDisposalService} 참고).
 */
@Service
public class MedicationReturnService {

    private static final String SOURCE_FORM_TYPE_RETURN = "RETURN";
    private static final int REASON_MAX_BYTES = 200;

    private final DispensingItemRepository dispensingItemRepository;
    private final MedicationReleaseRepository medicationReleaseRepository;
    private final MedicationReturnRepository medicationReturnRepository;
    private final MedicationReturnItemRepository medicationReturnItemRepository;
    private final StockMovementService stockMovementService;

    public MedicationReturnService(DispensingItemRepository dispensingItemRepository,
                                    MedicationReleaseRepository medicationReleaseRepository,
                                    MedicationReturnRepository medicationReturnRepository,
                                    MedicationReturnItemRepository medicationReturnItemRepository,
                                    StockMovementService stockMovementService) {
        this.dispensingItemRepository = dispensingItemRepository;
        this.medicationReleaseRepository = medicationReleaseRepository;
        this.medicationReturnRepository = medicationReturnRepository;
        this.medicationReturnItemRepository = medicationReturnItemRepository;
        this.stockMovementService = stockMovementService;
    }

    @Transactional
    public MedicationReturnItem create(MedicationReturnCreateRequest request) {
        if (request.reason().getBytes(StandardCharsets.UTF_8).length > REASON_MAX_BYTES) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        DispensingItem dispensingItem = dispensingItemRepository.findById(request.dispensingItemId())
                .orElseThrow(() -> new BusinessException(ErrorCode.DISPENSING_NOT_FOUND));

        // 한 조제상세를 여러 번에 나눠 반납할 수 있어 이번 요청 수량만 보면 안 되고,
        // 이전에 이미 반납된 수량까지 합쳐서 조제수량을 넘는지 확인해야 한다(안 그러면
        // 같은 조제상세를 반복 반납해서 재고가 중복으로 복구될 수 있다).
        BigDecimal alreadyReturned = medicationReturnItemRepository
                .findByDispensingItem_DispensingItemId(request.dispensingItemId())
                .stream()
                .map(MedicationReturnItem::getReturnQty)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (alreadyReturned.add(request.returnQty()).compareTo(dispensingItem.getDispensedQty()) > 0) {
            throw new BusinessException(ErrorCode.RETURN_QTY_EXCEEDS_DISPENSED);
        }

        MedicationRelease release = medicationReleaseRepository
                .findByDispensing_DispensingIdAndReleaseStatusCd(
                        dispensingItem.getDispensing().getDispensingId(), MedicationRelease.STATUS_RELEASED)
                .orElseThrow(() -> new BusinessException(ErrorCode.RELEASE_NOT_FOUND));

        MedicationReturn medicationReturn = medicationReturnRepository.save(
                new MedicationReturn(release, LocalDate.now(), request.reason(), request.actorId()));

        MedicationReturnItem item = medicationReturnItemRepository.save(
                new MedicationReturnItem(medicationReturn, dispensingItem, request.returnQty()));

        // 반납받은 수량은 일단 전부 원래 로트로 복구한다. 그중 폐기할 부분은 반납약품폐기
        // 단계에서 다시 차감된다(이 흐름이 PHM 지시서 3-5에 명시된 "반납 시 복구, 폐기 시 재차감" 그대로다).
        stockMovementService.increaseLot(
                dispensingItem.getMedicationLot(), request.returnQty(), StockMovementService.STOCK_TX_TYPE_RETURN,
                medicationReturn.getMedicationReturnId(), SOURCE_FORM_TYPE_RETURN, request.actorId());

        return item;
    }
}
