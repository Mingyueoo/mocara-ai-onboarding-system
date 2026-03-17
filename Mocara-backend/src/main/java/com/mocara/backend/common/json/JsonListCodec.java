package com.mocara.backend.common.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class JsonListCodec {

    private static final TypeReference<List<String>> LIST_OF_STRING = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public JsonListCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String toJson(List<String> items) {
        if (items == null) return null;
        try {
            return objectMapper.writeValueAsString(items);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to encode list as JSON", e);
        }
    }

    public List<String> fromJson(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, LIST_OF_STRING);
        } catch (Exception e) {
            // keep the API resilient: return empty rather than failing the whole request
            return Collections.emptyList();
        }
    }
}

