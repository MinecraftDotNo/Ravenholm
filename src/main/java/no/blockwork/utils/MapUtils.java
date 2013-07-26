package no.blockwork.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MapUtils {
    public static List<Object> getInverse(final Map map, final Object value) {
        final List<Object> keys = new ArrayList<>();

        for (final Object key : map.keySet()) {
            if (map.get(key).equals(value)) {
                keys.add(key);
            }
        }

        return keys;
    }
}
