package com.money.money_manager.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {
    private Long id;
    
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @NotNull(message = "Type is required")
    @Pattern(regexp = "EXPENSE|INCOME", message = "Type must be EXPENSE or INCOME")
    private String type;
    
    private String icon;
    
    @Pattern(regexp = "^#[A-Fa-f0-9]{6}$", message = "Color must be a valid hex color code")
    private String color = "#3B82F6";
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
