package com.money.money_manager.service;

import com.money.money_manager.dto.CategoryDTO;
import com.money.money_manager.entity.Category;
import com.money.money_manager.entity.User;
import com.money.money_manager.exception.ResourceNotFoundException;
import com.money.money_manager.repository.CategoryRepository;
import com.money.money_manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public CategoryDTO createCategory(Long userId, CategoryDTO categoryDTO) {
        log.info("Creating category for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (categoryRepository.existsByNameAndUserId(categoryDTO.getName(), userId)) {
            throw new IllegalArgumentException("Category with this name already exists");
        }

        Category category = modelMapper.map(categoryDTO, Category.class);
        category.setUser(user);
        category.setType(Category.CategoryType.valueOf(categoryDTO.getType().toUpperCase()));
        category.setCreatedAt(java.time.LocalDateTime.now());
        category.setUpdatedAt(java.time.LocalDateTime.now());

        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", savedCategory.getId());
        
        return modelMapper.map(savedCategory, CategoryDTO.class);
    }

    public CategoryDTO getCategoryById(Long userId, Long categoryId) {
        log.info("Fetching category with ID: {}", categoryId);
        
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        
        return modelMapper.map(category, CategoryDTO.class);
    }

    public List<CategoryDTO> getUserCategories(Long userId) {
        log.info("Fetching categories for user: {}", userId);
        return categoryRepository.findByUserId(userId).stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .collect(Collectors.toList());
    }

    public List<CategoryDTO> getCategoriesByType(Long userId, String type) {
        log.info("Fetching {} categories for user: {}", type, userId);
        Category.CategoryType categoryType = Category.CategoryType.valueOf(type.toUpperCase());
        return categoryRepository.findByUserIdAndType(userId, categoryType).stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .collect(Collectors.toList());
    }

    public CategoryDTO updateCategory(Long userId, Long categoryId, CategoryDTO categoryDTO) {
        log.info("Updating category with ID: {}", categoryId);
        
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (categoryDTO.getName() != null) {
            category.setName(categoryDTO.getName());
        }
        if (categoryDTO.getDescription() != null) {
            category.setDescription(categoryDTO.getDescription());
        }
        if (categoryDTO.getColor() != null) {
            category.setColor(categoryDTO.getColor());
        }
        if (categoryDTO.getIcon() != null) {
            category.setIcon(categoryDTO.getIcon());
        }

        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully with ID: {}", categoryId);
        
        return modelMapper.map(updatedCategory, CategoryDTO.class);
    }

    public void deleteCategory(Long userId, Long categoryId) {
        log.info("Deleting category with ID: {}", categoryId);
        
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        
        categoryRepository.delete(category);
        log.info("Category deleted successfully with ID: {}", categoryId);
    }
}
