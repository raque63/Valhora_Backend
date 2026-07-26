package com.valhora.backend.categories;

import com.valhora.backend.categories.dto.CategoryRequest;
import com.valhora.backend.categories.dto.CategoryResponse;
import com.valhora.backend.common.exception.DuplicateResourceException;
import com.valhora.backend.common.exception.ResourceNotFoundException;
import com.valhora.backend.common.util.Slugs;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("Ya existe una categoría con ese nombre");
        }
        Category category = Category.builder()
                .name(request.name())
                .slug(Slugs.slugify(request.name()))
                .build();
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(UUID id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
        category.setName(request.name());
        category.setSlug(Slugs.slugify(request.name()));
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoría no encontrada");
        }
        categoryRepository.deleteById(id);
    }
}
