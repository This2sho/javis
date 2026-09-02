package com.javis.learn_hub.support.i18n;

import java.util.Locale;

public enum ContentLanguage {
    KO,
    EN;

    public static ContentLanguage from(Locale locale) {
        if (locale != null && "en".equalsIgnoreCase(locale.getLanguage())) {
            return EN;
        }
        return KO;
    }

    public boolean isKorean() {
        return this == KO;
    }

    public boolean isEnglish() {
        return this == EN;
    }
}
