package com.finpro.roomio_backend.properties.repository;

import com.finpro.roomio_backend.properties.entity.Properties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PropertiesRepository extends JpaRepository<Properties, Long>, JpaSpecificationExecutor<Properties> {
    Optional<Properties> findByName(String name);

    // Search by name
    Page<Properties> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Search by city
    Page<Properties> findByCityIgnoreCase(String city, Pageable pageable);

    // Search by both name and city
    Page<Properties> findByNameContainingIgnoreCaseAndCityIgnoreCase(String name, String city, Pageable pageable);


}
