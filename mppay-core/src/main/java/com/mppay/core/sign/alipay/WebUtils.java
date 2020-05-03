package com.mppay.core.sign.alipay;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 网络工具类。
 *
 * @author carver.gu
 * @since 1.0, Sep 12, 2009
 */
public abstract class WebUtils {

    private static final String     DEFAULT_CHARSET = AlipayConstants.CHARSET_UTF8;
    private static final String     METHOD_POST     = "POST";
    private static final String     METHOD_GET      = "GET";

    private WebUtils() {
    }

    public static String buildQuery(Map<String, Object> params, String charset) throws IOException {
        if (params == null || params.isEmpty()) {
            return null;
        }

        StringBuilder query = new StringBuilder();
        Set<Entry<String, Object>> entries = params.entrySet();
        boolean hasParam = false;

        for (Entry<String, Object> entry : entries) {
            String name = entry.getKey();
            Object value = entry.getValue();
            // 忽略参数名或参数值为空的参数
            if (StringUtils.areNotEmpty(name) && null != value && StringUtils.areNotEmpty(value.toString())) {
                if (hasParam) {
                    query.append("&");
                } else {
                    hasParam = true;
                }

                query.append(name).append("=").append(URLEncoder.encode(value.toString(), charset));
            }
        }

        return query.toString();
    }
    
    public static Map<String, Object> buildQueryRetMap(Map<String, Object> params, String charset) throws IOException {
        if (params == null || params.isEmpty()) {
            return null;
        }

        Set<Entry<String, Object>> entries = params.entrySet();

        for (Entry<String, Object> entry : entries) {
            String name = entry.getKey();
            Object value = entry.getValue();
            // 忽略参数名或参数值为空的参数
            if (StringUtils.areNotEmpty(name, value.toString())) {
            	params.put(name, URLEncoder.encode(value.toString(), charset));
            }
        }

        return params;
    }

}
