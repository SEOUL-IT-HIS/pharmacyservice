package kr.co.seoulit.his.pharmacyservice.supplier.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(schema = "PHARMACY", name = "SUPPLIER")
public class Supplier {

    @Id
    @Column(name = "SUPPLIER_ID", length = 36)
    private String supplierId;

    @Column(name = "SUPPLIER_NAME", length = 200)
    private String supplierName;

    @Column(name = "CONTACT_PHONE", length = 50)
    private String contactPhone;

    @CreationTimestamp
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    protected Supplier() {
    }

    public Supplier(String supplierName, String contactPhone) {
        this.supplierId = UUID.randomUUID().toString();
        this.supplierName = supplierName;
        this.contactPhone = contactPhone;
    }
}
