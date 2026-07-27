package com.akbar.ecommerce_backend.user.dto.common;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PageResponseDTO<T> {

    @Schema(description = "List of records for the current page")
    private List<T> content;

    @Schema(
            description = "Current page number (0-based index)",
            example = "0"
    )
    private int currentPage;

    @Schema(
            description = "Maximum number of records per page",
            example = "20"
    )
    private int pageSize;

    @Schema(
            description = "Total number of records available",
            example = "125"
    )
    private long totalElements;

    @Schema(
            description = "Total number of available pages",
            example = "7"
    )
    private int totalPages;

    @Schema(
            description = "Indicates whether the current page is the first page",
            example = "true"
    )
    private boolean first;

    @Schema(
            description = "Indicates whether the current page is the last page",
            example = "false"
    )
    private boolean last;


}
