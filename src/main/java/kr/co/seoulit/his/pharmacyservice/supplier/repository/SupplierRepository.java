package kr.co.seoulit.his.pharmacyservice.supplier.repository;

import kr.co.seoulit.his.pharmacyservice.supplier.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, String> {

    List<Supplier> findAllByOrderBySupplierNameAsc();
}
