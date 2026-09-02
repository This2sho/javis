package com.javis.learn_hub.category.domain;

import com.javis.learn_hub.support.i18n.ContentLanguage;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public enum MainCategory {
    COMPUTER_SCIENCE("computer_science"),
    BACKEND("backend"),
    SYSTEM_DESIGN("system_design"),
    CULTURE_FIT("culture_fit"),
    ENGLISH_CONVERSATION("english_conversation", ContentLanguage.EN);

    private final String path;
    private final ContentLanguage preferredContentLanguage;

    MainCategory(String path) {
        this(path, null);
    }

    MainCategory(String path, ContentLanguage preferredContentLanguage) {
        this.path = path;
        this.preferredContentLanguage = preferredContentLanguage;
    }

    private static final Map<String, MainCategory> PATH_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toMap(
                            MainCategory::getPath,
                            Function.identity()
                    ));

    public static MainCategory from(String path) {
        MainCategory category = PATH_MAP.get(path.toLowerCase());
        if (category == null) {
            throw new IllegalArgumentException("존재하지 않는 메인카테고리입니다.");
        }
        return category;
    }

    public ContentLanguage resolveContentLanguage(ContentLanguage requestedContentLanguage) {
        return preferredContentLanguage == null ? requestedContentLanguage : preferredContentLanguage;
    }
}
