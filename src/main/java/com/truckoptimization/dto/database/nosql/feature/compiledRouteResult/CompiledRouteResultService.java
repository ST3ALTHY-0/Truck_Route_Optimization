package com.truckoptimization.dto.database.nosql.feature.compiledRouteResult;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.truckoptimization.dto.database.sql.features.user.AppUserEntity;
import com.truckoptimization.dto.database.sql.features.user.AppUserRepository;
import com.truckoptimization.dto.results.CompiledResults;

@Service
public class CompiledRouteResultService {

	private final CompiledRouteResultRepository compiledRouteResultRepository;
	private final AppUserRepository appUserRepository;

	public CompiledRouteResultService(CompiledRouteResultRepository compiledRouteResultRepository,
			AppUserRepository appUserRepository) {
		this.compiledRouteResultRepository = compiledRouteResultRepository;
		this.appUserRepository = appUserRepository;
	}

	public CompiledRouteResultDocument saveDocument(CompiledRouteResultDocument document) {
		if (document == null) {
			throw new IllegalArgumentException("CompiledRouteResultDocument cannot be null");
		}
		return compiledRouteResultRepository.save(document);
	}

	public CompiledResults saveCompiledResults(CompiledResults compiledResults) {
		if (compiledResults == null) {
			throw new IllegalArgumentException("CompiledResults cannot be null");
		}

		Long currentUserId = getCurrentAuthenticatedUserId();
		CompiledRouteResultDocument document = CompiledRouteResultDocument.fromDto(compiledResults, currentUserId);
		CompiledRouteResultDocument savedDocument = compiledRouteResultRepository.save(document);
		return savedDocument.toDto();
	}

	public Optional<CompiledRouteResultDocument> findById(String id) {
		return compiledRouteResultRepository.findById(id);
	}

	public Optional<CompiledResults> findCompiledResultsById(String id) {
		return compiledRouteResultRepository.findById(id).map(CompiledRouteResultDocument::toDto);
	}

	public List<CompiledRouteResultDocument> findAllDocuments() {
		return compiledRouteResultRepository.findAllByUserIdOrderByCreatedAtDesc(getCurrentAuthenticatedUserId());
	}

	public List<CompiledResults> findAllCompiledResults() {
		return compiledRouteResultRepository.findAllByUserIdOrderByCreatedAtDesc(getCurrentAuthenticatedUserId())
				.stream()
				.map(CompiledRouteResultDocument::toDto)
				.toList();
	}

	public Optional<CompiledRouteResultDocument> findLatestDocument() {
		return compiledRouteResultRepository
				.findTopByUserIdOrderByCreatedAtDesc(getCurrentAuthenticatedUserId());
	}

	public Optional<CompiledResults> findLatestCompiledResults() {
		return compiledRouteResultRepository.findTopByUserIdOrderByCreatedAtDesc(getCurrentAuthenticatedUserId())
				.map(CompiledRouteResultDocument::toDto);
	}

	public void deleteById(String id) {
		compiledRouteResultRepository.deleteById(id);
	}

	private Long getCurrentAuthenticatedUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			throw new IllegalStateException("No authenticated user found for saving compiled results.");
		}

		String username = authentication.getName();
		AppUserEntity user = appUserRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalStateException("Authenticated user not found in SQL store: " + username));

		if (user.getUserId() == null) {
			throw new IllegalStateException("Authenticated user does not have a userId assigned: " + username);
		}

		return user.getUserId();
	}
}
