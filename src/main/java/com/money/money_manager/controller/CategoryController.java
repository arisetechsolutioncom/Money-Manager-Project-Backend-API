package com.money.money_manager.controller;

import com.money.money_manager.dto.ApiResponse;
import com.money.money_manager.dto.CategoryDTO;
import com.money.money_manager.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<?> createCategory(
            @RequestHeader("userId") Long userId,
            @Valid @RequestBody CategoryDTO categoryDTO) {
        log.info("Creating category for user: {}", userId);
        CategoryDTO created = categoryService.createCategory(userId, categoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Category created successfully", created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCategory(
            @RequestHeader("userId") Long userId,
            @PathVariable Long id) {
        log.info("Fetching category with ID: {}", id);
        CategoryDTO category = categoryService.getCategoryById(userId, id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Category retrieved successfully", category));
    }

    @GetMapping
    public ResponseEntity<?> getUserCategories(@RequestHeader("userId") Long userId) {
        log.info("Fetching all categories for user: {}", userId);
        List<CategoryDTO> categories = categoryService.getUserCategories(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Categories retrieved successfully", categories));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<?> getCategoriesByType(
            @RequestHeader("userId") Long userId,
            @PathVariable String type) {
        log.info("Fetching {} categories for user: {}", type, userId);
        List<CategoryDTO> categories = categoryService.getCategoriesByType(userId, type);
        return ResponseEntity.ok(new ApiResponse<>(true, "Categories retrieved successfully", categories));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(
            @RequestHeader("userId") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO categoryDTO) {
        log.info("Updating category with ID: {}", id);
        CategoryDTO updated = categoryService.updateCategory(userId, id, categoryDTO);
        return ResponseEntity.ok(new ApiResponse<>(true, "Category updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(
            @RequestHeader("userId") Long userId,
            @PathVariable Long id) {
        log.info("Deleting category with ID: {}", id);
        categoryService.deleteCategory(userId, id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Category deleted successfully", null));
    }
}
