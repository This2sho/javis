package com.javis.learn_hub.support.repository;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.category.domain.repository.CategoryRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class InMemoryCategoryRepository extends InMemoryRepository<Category> implements CategoryRepository {

    @Override
    public List<Category> findAllByPathStartingWith(String mainCategoryName) {
        return findAll(category -> category.getPath().startsWith(mainCategoryName.toLowerCase()));
    }

    @Override
    public Optional<Category> findByPath(String path) {
        return findOne(category -> category.getPath().equals(path.toLowerCase()));
    }

    @Override
    public List<Category> findAllByIdIn(Set<Long> categoryIds) {
        return findAll(category -> categoryIds.contains(category.getId()));
    }

    @Override
    public List<Category> findAll() {
        return findAll(category -> true);
    }
}
