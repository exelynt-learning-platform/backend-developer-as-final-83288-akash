package com.bookingSystem.helper;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        description = "Paginated response containing a list of results and pagination metadata"
)
public record PageResponse<T>(

        @Schema(
                description = "List of records returned for the current page"
        )
        List<T> content,

        @Schema(
                description = "Zero-based page number",
                example = "0"
        )
        int page,

        @Schema(
                description = "Number of records requested per page",
                example = "10"
        )
        int size,

        @Schema(
                description = "Total number of records available across all pages",
                example = "25"
        )
        long totalElements,

        @Schema(
                description = "Total number of pages available",
                example = "3"
        )
        int totalPages
) {
}