package kr.co.seoulit.his.pharmacyservice.storagelocation.entity;

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
@Table(schema = "PHARMACY", name = "STORAGE_LOCATION")
public class StorageLocation {

    @Id
    @Column(name = "STORAGE_LOCATION_ID", length = 36)
    private String storageLocationId;

    @Column(name = "LOCATION_NAME", length = 200)
    private String locationName;

    @CreationTimestamp
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    protected StorageLocation() {
    }

    public StorageLocation(String locationName) {
        this.storageLocationId = UUID.randomUUID().toString();
        this.locationName = locationName;
    }
}
