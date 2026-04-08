package com.truckoptimization.dto.database.nosql.feature.matrixCache;

import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class MatrixService {

    private final MatrixRepository matrixRepository;

    public MatrixService(MatrixRepository matrixRepository) {
        this.matrixRepository = matrixRepository;
    }

    public MatrixDocument saveDocument(MatrixDocument document) {
        if (document == null) {
            throw new IllegalArgumentException("MatrixDocument cannot be null");
        }
        return matrixRepository.save(document);
    }

    public MatrixDocument findById(String id) {
        return matrixRepository.findById(id).orElse(null);
    }

    public void deleteById(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        matrixRepository.deleteById(id);
    }

    public Optional<MatrixDocument> findByWaypointsHash(Integer hash) {
        if (hash == null) {
            throw new IllegalArgumentException("Hash cannot be null or empty");
        }
        return matrixRepository.findByWaypointsHash(hash);
    }


}
