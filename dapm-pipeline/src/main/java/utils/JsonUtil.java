package utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String encode(String str) {
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }

    public static String decode(String str) {
        return URLDecoder.decode(str, StandardCharsets.UTF_8);
    }

    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON string. Object: " + obj, e);
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> typeRef) {
        try {
            return mapper.readValue(json, typeRef);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize JSON to object" + json, e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize JSON to object: " + json , e);
        }
    }
}
