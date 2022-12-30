package com.netease.hz.bdms.easyinsight.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class JsonUtils {

    private JsonUtils() {}

    /**
     * 工具类全局 ObjectMapper，线程安全
     */
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        init(objectMapper);
    }

    public static void init(ObjectMapper objectMapper) {
        if (objectMapper == null) {
            return;
        }
        // 反序列化时出现未知属性不报错
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 为 null 的属性不参与序列化
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 允许出现特殊字符和转义符
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        // 允许注释的出现
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        // 允许非双引号属性名
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        // 允许单引号来包住属性名称和字符串值
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        // 可解析反斜杠引用的所有字符
        objectMapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
    }

    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("JsonUtils: toJson fail, object: {}, cause: {}", object, e.getMessage());
            throw new RuntimeException("JsonUtils: error encoding json from " + object.getClass(), e);
        }
    }

    public static <T> T toObject(String json, Class<T> cls) {
        try {
            return objectMapper.readValue(json, cls);
        } catch (Exception e) {
            log.error("JsonUtils: toObject fail, json: {}, cls: {}, cause: {}", json, cls, e.getMessage());
            throw new RuntimeException("JsonUtils: error decode json to " + cls, e);
        }
    }

    /**
     * json string 转 obj，可转容器类
     */
    public static <T> T toObjectWithTypeRef(String json, TypeReference<T> toValueTypeRef) {
        try {
            return objectMapper.readValue(json, toValueTypeRef);
        } catch (Exception e) {
            log.error("JsonUtils: toObjectWithTypeRef fail, json: {}, typeRef: {}, cause: {}", json, toValueTypeRef, e.getMessage());
            throw new RuntimeException("JsonUtils: error decode json to " + toValueTypeRef, e);
        }
    }

    public static JsonNode getNode(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            log.error("JsonUtils: getNode fail, json: {}, cause: {}", json, e.getMessage());
            throw new RuntimeException("JsonUtils: getNode fail", e);
        }
    }

    public static JsonNode getNode(String json, String... paths) {
        try {
            JsonNode node = objectMapper.readTree(json);
            for (String path : paths) {
                node = node.path(path);
            }
            return node;
        } catch (Exception e) {
            log.error("JsonUtils: getNode(path...) fail, json: {}, cause: {}", json, e.getMessage());
            throw new RuntimeException("JsonUtils: getNode(path...) fail", e);
        }
    }

    public static <T> T toObject(JsonNode node, Class<T> cls) {
        try {
            return objectMapper.treeToValue(node, cls);
        } catch (Exception e) {
            log.error("JsonUtils: toObject(JsonNode) fail, node: {}, cause: {}", node, e.getMessage());
            throw new RuntimeException("JsonUtils: toObject(JsonNode) fail", e);
        }
    }

    /**
     * 将json字符串转换为Object对象
     *
     * @param jsonStr json字符串
     * @param clazz 要转换成的类
     * @param <T> 泛型
     * @return 转换后的结果
     */
    public static <T> T parseObject(String jsonStr, Class<T> clazz) {
        if (StringUtils.isBlank(jsonStr)) {
            return null;
        }
        try {
            return objectMapper.readValue(jsonStr, clazz);
        } catch (Exception e) {
            throw new RuntimeException("deserialize error", e);
        }
    }

    /**
     * 将json字符串转换为Object对象
     *
     * @param jsonStr json字符串
     * @param type 要转换成的类
     * @param <T> 泛型
     * @return 转换后的结果
     */
    public static <T> T parseObject(String jsonStr, TypeReference<T> type) {
        try {
            return objectMapper.readValue(jsonStr, type);
        } catch (IOException e) {
            throw new RuntimeException("deserialize error", e);
        }
    }

    /**
     * 将一个json字符串转换成map对象
     *
     * @param jsonStr 要转换的json字符串
     * @param <V> value泛型
     * @return 转换后的map结果
     */
    public static <V> Map<String, V> parseMap(String jsonStr) {
        if(StringUtils.isBlank(jsonStr)){
            return Maps.newHashMap();
        }
        try {
            return objectMapper.readValue(jsonStr, new TypeReference<Map<String, V>>() {});
        } catch (Exception e) {
            throw new RuntimeException("deserialize error", e);
        }
    }

    /**
     * 将json字符串转换成Object数组
     *
     * @param jsonStr json字符串
     * @param clazz 要转换成的类
     * @param <T> 泛型
     * @return 转换后的结果
     */
    public static <T> List<T> parseList(String jsonStr, Class<T> clazz) {
        if (StringUtils.isBlank(jsonStr)) {
            return Lists.newArrayList();
        }
        try {
            CollectionType collectionType = objectMapper.getTypeFactory()
                .constructCollectionType(ArrayList.class, clazz);
            return objectMapper.readValue(jsonStr, collectionType);
        } catch (Exception e) {
            throw new RuntimeException("deserialize error", e);
        }
    }

    /**
     * 将json字符串转换成Object数组
     *
     * @param jsonStr json字符串
     * @return 转换后的结果
     */
    public static List<Map<String,Object>> parseList(String jsonStr) {
        if (StringUtils.isBlank(jsonStr)) {
            return Lists.newArrayList();
        }
        try {
            return parseObject(jsonStr, new TypeReference<List<Map<String,Object>>>(){});
        } catch (Exception e) {
            throw new RuntimeException("deserialize error", e);
        }
    }
}