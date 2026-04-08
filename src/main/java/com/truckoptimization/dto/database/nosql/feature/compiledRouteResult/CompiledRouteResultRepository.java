package com.truckoptimization.dto.database.nosql.feature.compiledRouteResult;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompiledRouteResultRepository extends MongoRepository<CompiledRouteResultDocument, String> {

	List<CompiledRouteResultDocument> findAllByOrderByCreatedAtDesc();

	List<CompiledRouteResultDocument> findAllByUserIdOrderByCreatedAtDesc(Long userId);

	Optional<CompiledRouteResultDocument> findTopByOrderByCreatedAtDesc();

	Optional<CompiledRouteResultDocument> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}