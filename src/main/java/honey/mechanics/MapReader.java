package honey.mechanics;

import java.util.HashMap;
import java.util.Map;

public class MapReader {
    public static <T> T getOrDefault(Map<String, ?> map, String key, T defaultValue) {
        Object value = map.get(key);

        if (value != null) {
            if (value.getClass() == defaultValue.getClass()) {
                return (T) value;
            }
        }

        return defaultValue;
    }

    public static Number getNumberOrDefault(Map<String, Object> map, String key, Number defaultValue) {
        if (map.get(key) instanceof Number) {
            return (Number) map.get(key);
        }
        return defaultValue;
    }

    public static <T> T getOrGetDefault(Map<String, ?> map1, Map<String, ?> map2, String key, T defautlValue) {
        Object value1 = map1.get(key);
        if (value1 != null) {
            if (value1.getClass() == defautlValue.getClass()) {
                return (T) value1;
            }
        }

        Object value2 = map2.get(key);
        if (value2 != null) {
            if (value2.getClass() == defautlValue.getClass()) {
                return (T) value2;
            }
        }

        return defautlValue;
    }

    public static <T> T getOrGet(Map<String, ?> map1, Map<String, T> map2, String key) {
        Object value1 = map1.get(key);
        T value2 = map2.get(key);
        if (value1 != null) {
            if (value1.getClass() == value2.getClass()) {
                return (T) value1;
            }
        }
        return value2;
    }

    public static <T> Map<String, T> putAllIfAbsent(Map<String, T> map1, Map<String, T> map2) {
        for (String key : map2.keySet()) {
            map1.putIfAbsent(key, map2.get(key));
        }

        return map1;
    }

    public static <T> Map<String, T> castMap(Map<String, ?> map, Class<T> type) {
        Map<String, T> result = new HashMap<>();
        for (String key : map.keySet()) {
            Object value = map.get(key);
            if (value.getClass() == type) {
                result.put(key, (T) value);
            }
        }
        return result;
    }
}
