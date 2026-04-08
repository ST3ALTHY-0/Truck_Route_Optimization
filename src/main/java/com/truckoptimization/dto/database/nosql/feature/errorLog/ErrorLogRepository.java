package com.truckoptimization.dto.database.nosql.feature.errorLog;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErrorLogRepository extends MongoRepository<ErrorLogDocument, String> {
    
}
