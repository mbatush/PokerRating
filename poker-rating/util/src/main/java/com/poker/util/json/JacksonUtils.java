package com.poker.util.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonUtils {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private JacksonUtils() {
    throw new UnsupportedOperationException("No instance for util class");
  }

  public static <T> T fromJson(String json, Class<T> valueType) {
    try {
      return OBJECT_MAPPER.readValue(json, valueType);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public static String toJson(Object value) {
    try {
      return OBJECT_MAPPER.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
