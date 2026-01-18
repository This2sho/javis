package com.javis.learn_hub.category.domain.repository;

import com.javis.learn_hub.category.domain.Category;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.repository.Repository;

public interface CategoryRepository extends Repository<Category, Long> {

    Category save(Category category);

    List<Category> findAllByPathStartingWith(String mainCategoryName);

    Optional<Category> findByPath(String path);

    List<Category> findAllByIdIn(Set<Long> categoryIds);

    List<Category> findAll();

    Optional<Category> findById(Long id);
}
