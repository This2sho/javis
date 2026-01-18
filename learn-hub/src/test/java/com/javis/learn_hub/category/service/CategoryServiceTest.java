package com.javis.learn_hub.category.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.category.domain.repository.CategoryRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class CategoryServiceTest {

    @MockitoBean
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;

    @DisplayName("존재하지 않는 카테고리의 경우 새로 생성한다.")
    @Test
    void testGetOrCreate() {
        //given, when
        categoryService.getOrCreate(MainCategory.COMPUTER_SCIENCE.name(), "network", "tcp");

        //then
        verify(categoryRepository, times(1)).save(any());
    }

    @DisplayName("존재하는 카테고리의 경우 가져온다.")
    @Test
    void testGetOrCreate2() {
        //given
        MainCategory computerScience = MainCategory.COMPUTER_SCIENCE;
        String[] subCategories = {"network", "tcp"};
        Category existing = Category.of(computerScience, subCategories);

        given(categoryRepository.findByPath(existing.getPath())).willReturn(Optional.of(existing));

        // when
        categoryService.getOrCreate(computerScience.name(), subCategories);

        //then
        verify(categoryRepository, never()).save(any());
    }
}
