package com.javis.learn_hub.support.infrastructure;

import com.javis.learn_hub.support.domain.Association;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AssociationConverter implements AttributeConverter<Association, Long> {

    @Override
    public Long convertToDatabaseColumn(Association attribute) {
        if (attribute == null) return Association.getEmpty().getId();
        return attribute.getId();
    }

    @Override
    public Association convertToEntityAttribute(Long dbData) {
        return Association.from(dbData);
    }
}
