package com.micromart.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Paginated response wrapper for list endpoints.
 * <p>
 * PEAA Pattern: Data Transfer Object (DTO)
 * Provides pagination metadata alongside the data for client-side pagination handling.
 * <p>
 * Contains:
 * - content: The actual list of items
 * - pageNumber: Current page (0-indexed)
 * - pageSize: Number of items per page
 * - totalElements: Total number of items across all pages
 * - totalPages: Total number of pages
 * - first/last: Boolean flags for first/last page
 *
 * @param <T> The type of items in the page
 * @see <a href="https://martinfowler.com/eaaCatalog/dataTransferObject.html">Data Transfer Object</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {

    /**
     * The list of items on the current page.
     */
    private List<T> content;

    /**
     * Current page number (0-indexed).
     */
    private int pageNumber;

    /**
     * Number of items per page.
     */
    private int pageSize;

    /**
     * Total number of items across all pages.
     */
    private long totalElements;

    /**
     * Total number of pages.
     */
    private int totalPages;

    /**
     * Whether this is the first page.
     */
    private boolean first;

    /**
     * Whether this is the last page.
     */
    private boolean last;

    /**
     * Whether there are any items.
     */
    private boolean empty;

    /**
     * Factory method to create PageResponse from Spring Data Page.
     *
     * @param page Spring Data Page object
     * @param <T>  Type of items
     * @return PageResponse containing page data and metadata
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }

    /**
     * Factory method to create PageResponse from a list with manual pagination info.
     *
     * @param content       The list of items
     * @param pageNumber    Current page number
     * @param pageSize      Items per page
     * @param totalElements Total number of items
     * @param <T>           Type of items
     * @return PageResponse containing the data
     */
    public static <T> PageResponse<T> of(List<T> content, int pageNumber, int pageSize, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        return PageResponse.<T>builder()
                .content(content)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(pageNumber == 0)
                .last(pageNumber >= totalPages - 1)
                .empty(content == null || content.isEmpty())
                .build();
    }

    /**
     * Factory method to create PageResponse directly from Spring Data Page.
     * Alias for {@link #from(Page)} for consistent API.
     *
     * @param page Spring Data Page object
     * @param <T>  Type of items
     * @return PageResponse containing page data and metadata
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        return from(page);
    }
}
