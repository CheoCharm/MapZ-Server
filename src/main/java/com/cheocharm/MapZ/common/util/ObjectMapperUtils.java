package com.cheocharm.MapZ.common.util;

import com.cheocharm.MapZ.common.exception.common.FailJsonProcessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


public class ObjectMapperUtils {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static <T> T readValue(String body, Class<T> classType) {
        try {
            return objectMapper.readValue(body, classType);
        } catch (JsonProcessingException e) {
            throw new FailJsonProcessException(e);
        }
    }
}
