package com.bachlinh.order.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bachlinh.order.exception.system.common.CriticalException;
import com.bachlinh.order.exception.system.common.JsonConvertException;

import java.io.IOException;
import java.net.URL;

public final class JacksonUtils {
    private JacksonUtils() {
    }

    private static final ObjectMapper SINGLETON = new ObjectMapper();

    public static byte[] writeObjectAsBytes(Object value) {
        try {
            return SINGLETON.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new JsonConvertException("Can not convert [" + value.getClass().getSimpleName() + "] to json object", e);
        }
    }

    public static String writeObjectAsString(Object value) {
        try {
            return SINGLETON.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new JsonConvertException("Can not convert object to json");
        }
    }

    public static JsonNode readJsonFile(URL url) {
        try {
            return SINGLETON.readTree(url);
        } catch (IOException e) {
            throw new CriticalException(String.format("Can not read json from url [%s]", url.getPath()));
        }
    }

    public static ObjectMapper getSingleton() {
        return SINGLETON;
    }
}
