package com.javis.learn_hub.problem.domain.infrastructure;

import com.javis.learn_hub.problem.domain.Keywords;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Set;
import java.util.StringJoiner;

@Converter
public class KeywordsConverter implements AttributeConverter<Keywords, String> {

    @Override
    public String convertToDatabaseColumn(Keywords attribute) {
        Set<String> keywords = attribute.getKeywords();
        StringJoiner stringJoiner = new StringJoiner(Keywords.getDelimiter());
        for (String keyword : keywords) {
            stringJoiner.add(keyword);
        }
        return stringJoiner.toString();
    }

    @Override
    public Keywords convertToEntityAttribute(String dbData) {
        String[] keywords = dbData.split(Keywords.getDelimiter());
        return Keywords.from(Set.of(keywords));
    }
}
