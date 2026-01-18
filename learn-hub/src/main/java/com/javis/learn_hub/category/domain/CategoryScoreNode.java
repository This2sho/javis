package com.javis.learn_hub.category.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CategoryScoreNode implements CategoryNode {

    private String categoryName;
    private Map<String, CategoryScoreNode> children = new LinkedHashMap<>();
    private int selfScore;
    private int totalScore;

    private CategoryScoreNode(String categoryName) {
        this.categoryName = categoryName;
    }

    public static CategoryScoreNode from(Map<Category, Integer> scoresByCategory) {
        CategoryScoreNode root = new CategoryScoreNode("root");
        root.initChildren(scoresByCategory);
        CategoryScoreNode mainCategoryNode = root.getMainCategoryNode();
        mainCategoryNode.calculateTotalScore();
        return mainCategoryNode;
    }

    public static CategoryNode from(List<Category> categories) {
        CategoryScoreNode root = new CategoryScoreNode("root");
        root.initChildren(categories);
        return root.getMainCategoryNode();
    }

    private void initChildren(List<Category> categories) {
        for (Category category : categories) {
            traverseAndCreate(category);
        }
    }

    private void initChildren(Map<Category, Integer> scoresByCategory) {
        for (var scoreByCategory : scoresByCategory.entrySet()) {
            Category category = scoreByCategory.getKey();
            int score = scoreByCategory.getValue();

            CategoryScoreNode leaf = traverseAndCreate(category);
            leaf.selfScore = score;
        }
    }

    private CategoryScoreNode traverseAndCreate(Category category) {
        String[] categoryNames = category.getPath().split(Category.getDelimiter());
        CategoryScoreNode current = this;

        for (String categoryName : categoryNames) {
            current = current.children.computeIfAbsent(
                    categoryName,
                    CategoryScoreNode::new
            );
        }
        return current;
    }

    private CategoryScoreNode getMainCategoryNode() {
        return children.values()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("메인 카테고리가 존재하지 않습니다."));
    }

    private int calculateTotalScore() {
        int sum = this.selfScore;

        for (CategoryScoreNode child: this.children.values()) {
            sum += child.calculateTotalScore();
        }
        this.totalScore = sum;
        return sum;
    }

    @Override
    public String getCategoryName() {
        return categoryName;
    }

    public int getTotalScore() {
        return totalScore;
    }

    @Override
    public Map<String, CategoryScoreNode> getChildren() {
        return Collections.unmodifiableMap(children);
    }
}
