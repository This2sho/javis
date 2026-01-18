package com.javis.learn_hub.category.domain;

import com.javis.learn_hub.support.TestFixtureFactory;
import com.javis.learn_hub.support.builder.CategoryBuilder;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CategoryScoreNodeTest {

    @DisplayName("동일한 메인 카테고리들로 이름을 값으로 하는 트리형태를 생성한다.")
    @Test
    void testCategoryNode() {
        //given
        MainCategory mainCategory = MainCategory.COMPUTER_SCIENCE;
        String network = "network";
        Category category1 = CategoryBuilder.builder().withMainCategory(mainCategory)
                .withSubCategories(network)
                .build();
        String tcp = "tcp";
        Category category2 = CategoryBuilder.builder().withMainCategory(mainCategory)
                .withSubCategories(network, tcp)
                .build();
        String database = "database";
        Category category3 = CategoryBuilder.builder().withMainCategory(mainCategory)
                .withSubCategories(database)
                .build();
        List<Category> categoryInput = List.of(category1, category2, category3);

        //when
        CategoryNode categoryNode = CategoryScoreNode.from(categoryInput);

        //then
        SoftAssertions.assertSoftly(softly -> {
            CategoryNode subCategoryNode1 = categoryNode.getChildren().get(network);
            CategoryNode subCategoryNode2 = categoryNode.getChildren().get(database);
            CategoryNode subCategoryNode1SubCategoryNode = subCategoryNode1.getChildren().get(tcp);

            softly.assertThat(categoryNode.getCategoryName()).isEqualTo(mainCategory.getPath());
            softly.assertThat(subCategoryNode1.getCategoryName()).isEqualTo(network);
            softly.assertThat(subCategoryNode1.getChildren().size()).isOne();
            softly.assertThat(subCategoryNode2.getCategoryName()).isEqualTo(database);
            softly.assertThat(subCategoryNode2.getChildren().size()).isZero();
            softly.assertThat(subCategoryNode1SubCategoryNode.getCategoryName()).isEqualTo(tcp);
            softly.assertThat(subCategoryNode1SubCategoryNode.getChildren().size()).isZero();
        });
    }

    @DisplayName("카테고리와 각 카테고리의 점수로 트리를 생성하고 상위 카테고리 점수는 하위 카테고리 점수를 포함한다.")
    @Test
    void testCategoryScoreNode() {
        //given
        TestFixtureFactory testFixtureFactory = new TestFixtureFactory();
        MainCategory mainCategory = MainCategory.COMPUTER_SCIENCE;
        String network = "network";
        Category cs_network = testFixtureFactory.make(CategoryBuilder.builder().withMainCategory(mainCategory)
                .withSubCategories(network)
                .build());
        int cs_network_score = 10;
        String tcp = "tcp";
        Category cs_network_tcp = testFixtureFactory.make(CategoryBuilder.builder().withMainCategory(mainCategory)
                .withSubCategories(network, tcp)
                .build());
        int cs_network_tcp_score = 20;
        String database = "database";
        Category cs_database = testFixtureFactory.make(CategoryBuilder.builder().withMainCategory(mainCategory)
                .withSubCategories(database)
                .build());
        int cs_database_score = 5;

        Map<Category, Integer> scoresByCategory = Map.of(cs_network, cs_network_score, cs_network_tcp, cs_network_tcp_score,
                cs_database, cs_database_score);

        int expectedTotalScore = cs_network_score + cs_network_tcp_score + cs_database_score;
        int expectedNetworkTotalScore = cs_network_score + cs_network_tcp_score;

        //when
        CategoryScoreNode categoryScoreNode = CategoryScoreNode.from(scoresByCategory);

        //then
        SoftAssertions.assertSoftly(softly -> {
            int actualTotalScore = categoryScoreNode.getTotalScore();
            CategoryScoreNode networkNode = categoryScoreNode.getChildren().get(network);
            int actualNetworkTotalScore = networkNode.getTotalScore();
            int actualTcpScore = networkNode.getChildren().get(tcp).getTotalScore();
            int actualDatabaseScore = categoryScoreNode.getChildren().get(database).getTotalScore();
            softly.assertThat(actualTotalScore).isEqualTo(expectedTotalScore);
            softly.assertThat(actualNetworkTotalScore).isEqualTo(expectedNetworkTotalScore);
            softly.assertThat(actualTcpScore).isEqualTo(cs_network_tcp_score);
            softly.assertThat(actualDatabaseScore).isEqualTo(cs_database_score);
        });
    }
}
