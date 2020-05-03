package com.mppay.core.utils;

import java.util.HashMap;
import java.util.Map;

public class MapUtil {

    /**
     * 根据参数来创建map
     *
     * @param o
     * @return
     */
    public static Map<String, Object> of(Object... o) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i < o.length; i++) {
            if (i % 2 == 0) {
                m.put(String.valueOf(o[i]), o[i + 1]);
            }
        }
        return m;
    }

    public static Map<String, String> ofString(Object... o) {
        Map<String, String> m = new HashMap<>();
        for (int i = 0; i < o.length; i++) {
            if (i % 2 == 0) {
                m.put(String.valueOf(o[i]), String.valueOf(o[i + 1]));
            }
        }
        return m;
    }
    
}
