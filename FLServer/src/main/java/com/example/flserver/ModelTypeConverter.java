package com.example.flserver;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ModelTypeConverter implements AttributeConverter<ModelType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(ModelType modelType){
        return modelType != null ? modelType.getCode() : null;
    }

    public ModelType convertToEntityAttribute(Integer dbCode){
        return dbCode != null ? ModelType.fromCode(dbCode) : null;
    }
}
