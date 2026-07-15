package com.javis.learn_hub.support.i18n;

import com.javis.learn_hub.category.domain.MainCategory;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MainCategoryLabelResolver {

    private final MessageSource messageSource;

    public String resolveByPath(String path, Locale locale) {
        MainCategory mainCategory = MainCategory.from(path);
        return resolve(mainCategory, locale);
    }

    public String resolve(MainCategory mainCategory, Locale locale) {
        return messageSource.getMessage(messageKey(mainCategory), null, locale);
    }

    public String messageKey(MainCategory mainCategory) {
        return "mainCategory." + mainCategory.getPath();
    }
}
