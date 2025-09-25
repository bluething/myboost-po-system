package io.github.bluething.myboostposystem.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
abstract class BaseController {
    /**
     * Create a successful response with data
     */
    protected <T> ResponseEntity<T> ok(T data) {
        return ResponseEntity.ok(data);
    }

    /**
     * Create a successful response for creation
     */
    protected <T> ResponseEntity<T> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(data);
    }

    /**
     * Create a successful response for deletion
     */
    protected ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    /**
     * Create a paginated response with metadata
     */
    protected <T> ResponseEntity<PagedResponse<T>> pagedResponse(Page<T> page) {
        PagedResponse<T> response = new PagedResponse<>(
                page.getContent(),
                new PageMetadata(
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalElements(),
                        page.getTotalPages(),
                        page.isFirst(),
                        page.isLast(),
                        page.hasNext(),
                        page.hasPrevious()
                )
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Log request information
     */
    protected void logRequest(String operation, Object... params) {
        log.info("Processing {}: {}", operation, params);
    }

    /**
     * Log response information
     */
    protected void logResponse(String operation, Object result) {
        log.debug("Completed {}: {}", operation, result != null ? result.getClass().getSimpleName() : "null");
    }

    /**
     * Validate pageable parameters
     */
    protected void validatePageable(Pageable pageable) {
        if (pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Page size cannot exceed 100");
        }
        if (pageable.getPageNumber() < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
    }
}
