package com.truckoptimization.dto.database.sql.features.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationEntityRepository extends JpaRepository<LocationEntity, Long> {
    
    // Custom method to find by address
    Optional<LocationEntity> findByAddress(String address);
}
