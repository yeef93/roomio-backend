package com.finpro.roomio_backend.properties.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rooms")
public class Rooms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "rooms_id_gen")
    @SequenceGenerator(name = "rooms_id_gen", sequenceName = "rooms_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private Properties properties ;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @NotNull
    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @NotNull
    @Column(name = "size")
    private Integer size;

    @Column(name = "bed_type", length = 100)
    private String bedType;

    @NotNull
    @Column(name = "total_bed")
    private Integer totalBed;

    @NotNull
    @Column(name = "total_bathroom")
    private Integer totalBathroom;

    @NotNull
    @Column(name = "qty")
    private Integer qty;

    @NotNull
    @Column(name = "base_price")
    private BigDecimal basePrice;

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
    }


}
