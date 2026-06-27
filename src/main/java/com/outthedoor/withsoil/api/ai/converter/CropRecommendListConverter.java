// 1. Converter 작성
package com.outthedoor.withsoil.api.ai.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.outthedoor.withsoil.api.ai.dto.response.CropRecommendDetailDto;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Converter
public class CropRecommendListConverter implements AttributeConverter<List<CropRecommendDetailDto>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<CropRecommendDetailDto> attribute) {
        try {
            return attribute == null ? null : objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @Override
    public List<CropRecommendDetailDto> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isEmpty()) {
                return Collections.emptyList();
            }
            return objectMapper.readValue(dbData, new TypeReference<List<CropRecommendDetailDto>>() {});
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }
}