package com.javis.learn_hub.support.builder;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.category.domain.MainCategory;

public class CategoryBuilder {

    private MainCategory mainCategory =  MainCategory.COMPUTER_SCIENCE;
    private String[] subCategories = new String[] {"network", "tcp"};

    public static CategoryBuilder builder() {
        return new CategoryBuilder();
    }

    public CategoryBuilder withMainCategory(MainCategory mainCategory) {
        this.mainCategory = mainCategory;
        return this;
    }

    public CategoryBuilder withSubCategories(String... subCategories) {
        this.subCategories = subCategories;
        return this;
    }

    public Category build() {
        return Category.of(mainCategory, subCategories);
    }
}

