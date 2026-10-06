package com.akbar.ecommerce_backend.category.service;

import com.akbar.ecommerce_backend.category.dto.CategoryRequestDTO;
import com.akbar.ecommerce_backend.category.dto.CategoryResponseDTO;
import com.akbar.ecommerce_backend.category.dto.CategoryUpdateRequestDTO;
import com.akbar.ecommerce_backend.category.enums.CategoryStatus;
import com.akbar.ecommerce_backend.common.PageResponseDTO;
import org.springframework.data.domain.Pageable;

public interface CategoryService {


    CategoryResponseDTO createCategory(CategoryRequestDTO requestDTO);

    PageResponseDTO<CategoryResponseDTO> getAllCategories(String keyword, CategoryStatus status, Pageable pageable);

    CategoryResponseDTO getCategoryById(Long id);

    CategoryResponseDTO updateCategory(Long id, CategoryUpdateRequestDTO requestDTO);

    void deleteCategory(Long id);

    CategoryResponseDTO restoreCategory(Long id);

}
