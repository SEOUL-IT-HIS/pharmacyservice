package kr.co.seoulit.his.pharmacyservice.storagelocation.repository;

import kr.co.seoulit.his.pharmacyservice.storagelocation.entity.StorageLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StorageLocationRepository extends JpaRepository<StorageLocation, String> {

    List<StorageLocation> findAllByOrderByLocationNameAsc();
}
