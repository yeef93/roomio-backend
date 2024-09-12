package com.finpro.roomio_backend.image.repository;

import com.finpro.roomio_backend.image.entity.ImageProperties;
import com.finpro.roomio_backend.properties.entity.Properties;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImagePropertiesRepository extends JpaRepository<ImageProperties, Long> {
    List<ImageProperties> findByProperties(Properties property);
}
