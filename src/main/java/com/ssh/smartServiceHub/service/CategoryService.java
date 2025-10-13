package com.ssh.smartServiceHub.service;

import com.ssh.smartServiceHub.dto.CategoryDTO;
import com.ssh.smartServiceHub.entity.Category;
import com.ssh.smartServiceHub.mapper.CategoryMapper;
import com.ssh.smartServiceHub.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryDTO createCategory(CategoryDTO newCategory) {
        Category category = CategoryMapper.toEntity(newCategory);
        Category savedCategory = categoryRepository.save(category);

        return CategoryMapper.toDTO(savedCategory);
    }

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        return CategoryMapper.toDTO(category);
    }

    public CategoryDTO updateCategory(Long id, CategoryDTO updateCategory) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        category.setName(updateCategory.getName());
        category.setDescription(updateCategory.getDescription());

        Category categoryUpdate = categoryRepository.save(category);
        return CategoryMapper.toDTO(categoryUpdate);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

}
