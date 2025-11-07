package com.hulkhiretech.payments.util;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class JsonUtil {

    private final ObjectMapper objectMapper;

    /**
     * Converts a Java object to JSON string.
     */
    public String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
        	log.error("Error converting object to JSON: {}", e.getMessage());
            throw new RuntimeException("Error converting object to JSON", e);
        }
    }

    /**
	 * Converts a JSON string to a Java object of the specified class.
	 */
    public <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
        	log.error("Error parsing JSON string to object: {}", e.getMessage());		
            throw new RuntimeException("Error parsing JSON string to object", e);
        }
    }
}