package com.backend.expensetracker_backend.service;

import com.backend.expensetracker_backend.dto.CategoryCreateDTO;
import com.backend.expensetracker_backend.dto.CategoryResponseDTO;

import java.util.List;

public interface CategoryService {

    CategoryResponseDTO createCategory(CategoryCreateDTO dto);

    List<CategoryResponseDTO> getAllActiveCategories();

    CategoryResponseDTO getCategoryById(Long id);
}