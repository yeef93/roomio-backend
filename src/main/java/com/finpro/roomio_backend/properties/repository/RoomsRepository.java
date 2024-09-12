package com.finpro.roomio_backend.properties.repository;


import com.finpro.roomio_backend.properties.entity.Rooms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomsRepository extends JpaRepository<Rooms, Long> {
}
