package com.javis.learn_hub.category.service;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.category.domain.CategoryNode;
import com.javis.learn_hub.category.domain.CategoryScoreNode;
import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.category.domain.repository.CategoryRepository;
import com.javis.learn_hub.category.service.dto.AllCategoryNodesResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public Category getOrCreate(String mainCategory, String... subCategories) {
        Category category = Category.of(MainCategory.valueOf(mainCategory.toUpperCase()), subCategories);
        return categoryRepository.findByPath(category.getPath())
                .orElseGet(() -> categoryRepository.save(category));
    }

    public AllCategoryNodesResponse getAllCategories() {
        List<Category> allCategories = categoryRepository.findAll();
        Map<MainCategory, List<Category>> categoriesByMainCategory = allCategories.stream()
                .collect(Collectors.groupingBy(Category::getMainCategory));

        List<CategoryNode> categoryNodes = new ArrayList<>();
        for (MainCategory mainCategory : categoriesByMainCategory.keySet()) {
            List<Category> categories = categoriesByMainCategory.get(mainCategory);
            CategoryNode categoryNode = CategoryScoreNode.from(categories);
            categoryNodes.add(categoryNode);
        }
        return AllCategoryNodesResponse.from(categoryNodes);
    }
}
