package com.javis.learn_hub.category.domain.service;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.category.domain.repository.CategoryRepository;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CategoryReader {

    private final CategoryRepository categoryRepository;

    public List<Category> getAllSubCategoriesFrom(MainCategory mainCategory) {
        return categoryRepository.findAllByPathStartingWith(mainCategory.getPath());
    }

    public List<Category> getAll(Set<Long> categoryIds) {
        return categoryRepository.findAllByIdIn(categoryIds);
    }

    public Category get(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 카테고리입니다."));
    }

    public Category get(String categoryPath) {
        return categoryRepository.findByPath(categoryPath)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 카테고리입니다."));
    }
}
