package com.backend.expensetracker_backend.service.impl;

import com.backend.expensetracker_backend.dto.CategoryCreateDTO;
import com.backend.expensetracker_backend.dto.CategoryResponseDTO;
import com.backend.expensetracker_backend.entity.Category;
import com.backend.expensetracker_backend.repository.CategoryRepository;
import com.backend.expensetracker_backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryResponseDTO createCategory(CategoryCreateDTO dto) {
        categoryRepository.findByName(dto.getName())
                .ifPresent(c -> {
                    throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + dto.getName());
                });

        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setActive(true);

        Category saved = categoryRepository.save(category);

        return mapToResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllActiveCategories() {
        return categoryRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con id: " + id));

        return mapToResponseDTO(category);
    }

    // Método auxiliar para mapear entidad a DTO
    private CategoryResponseDTO mapToResponseDTO(Category category) {
        return new CategoryResponseDTO(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getActive()
        );
    }
}