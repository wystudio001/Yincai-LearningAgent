package xyz.wystudio.yincai.core.common;

import java.util.List;
import java.util.Map;

/** 从后端的动态 Map 响应中读取稳定的展示文本。 */
public final class MapValues {
    private MapValues() { }
    public static String text(Map<String, Object> map, String key, String fallback) {
        if (map == null || map.get(key) == null) return fallback;
        String value = String.valueOf(map.get(key)).trim();
        return value.isEmpty() || "null".equalsIgnoreCase(value) ? fallback : value;
    }
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> maps(Map<String, Object> map, String key) {
        Object value = map == null ? null : map.get(key);
        return value instanceof List ? (List<Map<String, Object>>) value : List.of();
    }
}
