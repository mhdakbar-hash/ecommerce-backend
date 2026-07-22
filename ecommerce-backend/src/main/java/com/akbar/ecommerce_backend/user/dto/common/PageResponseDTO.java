package com.akbar.ecommerce_backend.user.dto.common;


import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PageResponseDTO<T> {

    private List<T> content;

    private int currentPage;

    private int pageSize;

    private long totalElements;

    private int totalPages;

    private boolean first;

    private boolean last;

}
