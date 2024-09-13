package com.finpro.roomio_backend.properties.repository;


import com.finpro.roomio_backend.properties.entity.Rooms;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomsRepository extends JpaRepository<Rooms, Long> {
    List<Rooms> findByPropertiesId(Long propertyId);
    Optional<Rooms> findByIdAndPropertiesId(Long roomId, Long propertyId);
//    @Query("SELECT MIN(r.basePrice) FROM Room r WHERE r.property.id = :propertyId AND r.isActive = true")
//    BigDecimal findMinBasePriceByPropertyId(@Param("propertyId") Long propertyId);
}
