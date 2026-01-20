package com.money.money_manager.repository;

import com.money.money_manager.entity.Category;
import com.money.money_manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserId(Long userId);
    Optional<Category> findByIdAndUserId(Long id, Long userId);
    Optional<Category> findByNameAndUserId(String name, Long userId);
    boolean existsByNameAndUserId(String name, Long userId);
    List<Category> findByUserIdAndType(Long userId, Category.CategoryType type);
}
