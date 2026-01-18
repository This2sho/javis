package com.javis.learn_hub.support;

import com.javis.learn_hub.category.domain.repository.CategoryRepository;
import com.javis.learn_hub.category.domain.service.CategoryReader;

public class SimpleCategoryReader extends CategoryReader {

    public SimpleCategoryReader(CategoryRepository categoryRepository) {
        super(categoryRepository);
    }
}
