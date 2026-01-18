package com.javis.learn_hub.category.domain;

import java.util.Map;

public interface CategoryNode {

    String getCategoryName();

    Map<String, ? extends CategoryNode> getChildren();
}
