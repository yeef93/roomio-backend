package com.finpro.roomio_backend.properties.repository;

import com.finpro.roomio_backend.properties.entity.Properties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PropertiesRepository extends JpaRepository<Properties, Long> {
    Optional<Properties> findByName(String name);
}
