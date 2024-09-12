package com.finpro.roomio_backend.image.entity;

import com.finpro.roomio_backend.properties.entity.Properties;
import com.finpro.roomio_backend.users.entity.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "image_properties")
public class ImageProperties {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "image_id_gen")
    @SequenceGenerator(name = "image_id_gen", sequenceName = "image_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "image_name", nullable = false)
    private String imageName;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "tenant_id")
    private Users user;  // The user who uploaded the image

    @ManyToOne
    @JoinColumn(name = "property_id")
    private Properties properties;  // The user who uploaded the image

    @ColumnDefault("true")
    @Column(name = "is_active")
    private Boolean isActive = true;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        deletedAt = Instant.now();
    }
}
