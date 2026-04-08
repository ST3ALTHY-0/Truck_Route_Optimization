package com.truckoptimization.dto.database.nosql.feature.matrixCache;

import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

@Repository
public interface MatrixRepository extends MongoRepository<MatrixDocument, String> {
    
    public Optional<MatrixDocument> findByWaypointsHash(Integer hash);
}
