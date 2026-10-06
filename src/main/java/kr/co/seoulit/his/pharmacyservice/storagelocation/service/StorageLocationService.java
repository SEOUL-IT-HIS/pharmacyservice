package kr.co.seoulit.his.pharmacyservice.storagelocation.service;

import kr.co.seoulit.his.pharmacyservice.storagelocation.dto.StorageLocationDto;
import kr.co.seoulit.his.pharmacyservice.storagelocation.dto.StorageLocationRegisterRequest;
import kr.co.seoulit.his.pharmacyservice.storagelocation.entity.StorageLocation;
import kr.co.seoulit.his.pharmacyservice.storagelocation.repository.StorageLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 보관위치 마스터 — 입고 등록 화면에서 자유 텍스트로 storageLocationId를 입력하던 것을 없애고
 * 실제 등록된 보관위치만 고를 수 있게 하기 위해 새로 만들었다(2026-10-06).
 */
@Service
@RequiredArgsConstructor
public class StorageLocationService {

    private final StorageLocationRepository storageLocationRepository;

    @Transactional(readOnly = true)
    public List<StorageLocationDto> getStorageLocationList() {
        return storageLocationRepository.findAllByOrderByLocationNameAsc()
                .stream()
                .map(StorageLocationService::toDto)
                .toList();
    }

    @Transactional
    public void registerStorageLocation(StorageLocationRegisterRequest request) {
        storageLocationRepository.save(new StorageLocation(request.getLocationName().trim()));
    }

    private static StorageLocationDto toDto(StorageLocation location) {
        StorageLocationDto dto = new StorageLocationDto();
        dto.setStorageLocationId(location.getStorageLocationId());
        dto.setLocationName(location.getLocationName());
        return dto;
    }
}
