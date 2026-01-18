package com.javis.learn_hub.category.domain.service;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.category.domain.repository.CategoryRepository;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CategoryProcessor {

    private final CategoryRepository categoryRepository;

    /**
     * 관리자가 json으로 생성할 때 사용
     * 사용자는 Reader만 사용
     */
    public Category makeIfAbsentByPath(String categoryPath) {
        String delimiter = Category.getDelimiter();
        String[] parts = categoryPath.split(delimiter);

        MainCategory mainCategory = MainCategory.from(parts[0]);
        StringBuilder currentPath = new StringBuilder(parts[0]);
        Category lastCategory = null;

        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                currentPath.append(delimiter).append(parts[i]);
            }

            String path = currentPath.toString();
            int depth = i;

            lastCategory = categoryRepository.findByPath(path)
                    .orElseGet(() -> {
                        String[] subCategories = Arrays.copyOfRange(parts, 1, depth + 1);
                        Category newCategory = Category.of(mainCategory, subCategories);
                        return categoryRepository.save(newCategory);
                    });
        }
        return lastCategory;
    }
}
