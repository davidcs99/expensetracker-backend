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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryResponseDTO createCategory(CategoryCreateDTO dto) {
        // Verificar que no exista una categoría con el mismo nombre
        categoryRepository.findByName(dto.getName())
                .ifPresent(c -> {
                    throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + dto.getName());
                });

        // Crear nueva categoría
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setActive(true);

        // Guardar
        Category saved = categoryRepository.save(category);

        // Convertir a DTO de respuesta
        return mapToResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllActiveCategories() {
        return categoryRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
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