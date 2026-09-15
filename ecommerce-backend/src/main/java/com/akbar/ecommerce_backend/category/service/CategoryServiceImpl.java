package com.akbar.ecommerce_backend.category.service;

import com.akbar.ecommerce_backend.category.dto.CategoryRequestDTO;
import com.akbar.ecommerce_backend.category.dto.CategoryResponseDTO;
import com.akbar.ecommerce_backend.category.dto.CategoryUpdateRequestDTO;
import com.akbar.ecommerce_backend.category.entity.CategoryEntity;
import com.akbar.ecommerce_backend.category.enums.CategoryStatus;
import com.akbar.ecommerce_backend.category.mapper.CategoryMapper;
import com.akbar.ecommerce_backend.category.repository.CategoryRepository;
import com.akbar.ecommerce_backend.category.specification.CategorySpecification;
import com.akbar.ecommerce_backend.common.PageResponseDTO;
import com.akbar.ecommerce_backend.exception.CategoryAlreadyExistsException;
import com.akbar.ecommerce_backend.exception.CategoryInUseException;
import com.akbar.ecommerce_backend.exception.ResourceNotFoundException;
import com.akbar.ecommerce_backend.product.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ProductRepository productRepository;


    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper,
            ProductRepository productRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.productRepository = productRepository;
    }




    @Override
    public CategoryResponseDTO createCategoty(CategoryRequestDTO requestDTO) {

        log.info("Creating category. Name: {}", requestDTO.getName());

        if (categoryRepository.existsByNameIgnoreCase(requestDTO.getName())) {
            throw new CategoryAlreadyExistsException("Category already exists with name: " + requestDTO.getName());
        }

        CategoryEntity category = categoryMapper.toEntity(requestDTO);

        category.setStatus(CategoryStatus.ACTIVE);

        CategoryEntity savedCategory =
                categoryRepository.save(category);

        log.info("Category created successfully. Category ID: {}, Name: {}",
                savedCategory.getId(),
                savedCategory.getName());

        return categoryMapper.toResponseDTO(savedCategory);

    }




    @Override
    public PageResponseDTO<CategoryResponseDTO> getAllCategories(String keyword, CategoryStatus status, Pageable pageable) {

        log.debug("Fetching categories. Keyword: {}, Status: {}, Page: {}, Size: {}",
                keyword,
                status,
                pageable.getPageNumber(),
                pageable.getPageSize());

        Specification<CategoryEntity> specification =
                CategorySpecification.isNotDeleted();

        if (keyword != null && !keyword.isBlank()) {

            specification = specification.and(
                    CategorySpecification.hasKeyword(keyword)
            );
        }

        if (status != null) {

            specification = specification.and(
                    CategorySpecification.hasStatus(status)
            );
        }

        Page<CategoryEntity> categories =
                categoryRepository.findAll(specification, pageable);


        log.debug("Fetched {} categories out of {} total categories. Page: {}, Size: {}",
                categories.getNumberOfElements(),
                categories.getTotalElements(),
                categories.getNumber(),
                categories.getSize());


        List<CategoryResponseDTO> response =
                categories.getContent()
                        .stream()
                        .map(categoryMapper::toResponseDTO)
                        .toList();

        return new PageResponseDTO<>(
                response,
                categories.getNumber(),
                categories.getSize(),
                categories.getTotalElements(),
                categories.getTotalPages(),
                categories.isFirst(),
                categories.isLast()
        );
    }





    @Override
    public CategoryResponseDTO getCategoryById(Long id) {

        CategoryEntity category = categoryRepository.findByIdAndStatusNot(id, CategoryStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("No category found with id: " + id));

        log.debug("Category fetched successfully. Category ID: {}, Name: {}",
                category.getId(),
                category.getName());

        return categoryMapper.toResponseDTO(category);
    }





    @Override
    public CategoryResponseDTO updateCategory(Long id, CategoryUpdateRequestDTO requestDTO) {

        CategoryEntity category = categoryRepository.findByIdAndStatusNot(id, CategoryStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("No category found with id: " + id));


        log.info("Updating category. Category ID: {}, Current Name: {}",
                category.getId(),
                category.getName());

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(
                requestDTO.getName(), id)) {

            throw new CategoryAlreadyExistsException("Category already exists with name: " + requestDTO.getName());
        }

        category.setName(requestDTO.getName());
        category.setDescription(requestDTO.getDescription());

        CategoryEntity updatedCategory =
                categoryRepository.save(category);

        log.info("Category updated successfully. Category ID: {}, Name: {}",
                updatedCategory.getId(),
                updatedCategory.getName());

        return categoryMapper.toResponseDTO(updatedCategory);
    }





    @Override
    public void deleteCategory(Long id) {

        CategoryEntity category = categoryRepository.findByIdAndStatusNot(id, CategoryStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("No category found with id: " + id));


        log.info("Soft delete requested. Category ID: {}, Name: {}",
                category.getId(),
                category.getName());


        if (productRepository.existsByCategoryId(id)) {

            throw new CategoryInUseException(
                    "Category cannot be deleted because products are associated with it");
        }


        category.setStatus(CategoryStatus.DELETED);

        categoryRepository.save(category);

        log.info("Category soft deleted successfully. Category ID: {}, Name: {}",
                category.getId(),
                category.getName());

    }




    @Override
    public CategoryResponseDTO restoreCategory(Long id) {

        CategoryEntity category = categoryRepository.findByIdAndStatus(id, CategoryStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("No deleted category found with id: " + id));


        log.info("Restore requested. Category ID: {}, Name: {}",
                category.getId(),
                category.getName());


        category.setStatus(CategoryStatus.ACTIVE);

        CategoryEntity restoredCategory =
                categoryRepository.save(category);

        log.info("Category restored successfully. Category ID: {}, Name: {}",
                restoredCategory.getId(),
                restoredCategory.getName());

        return categoryMapper.toResponseDTO(restoredCategory);

    }


}
