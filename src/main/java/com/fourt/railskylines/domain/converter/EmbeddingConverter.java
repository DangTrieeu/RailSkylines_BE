package com.fourt.railskylines.domain.converter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EmbeddingConverter implements AttributeConverter<List<Double>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Double> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(
                    attribute.stream()
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize embedding vector", exception);
        }
    }

    @Override
    public List<Double> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Collections.emptyList();
        }
        try {
            double[] raw = MAPPER.readValue(dbData, double[].class);
            return java.util.Arrays.stream(raw)
                    .boxed()
                    .collect(Collectors.toList());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to deserialize embedding vector", exception);
        }
    }
}
