package com.mppay.core.utils;

import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author feihang.chen Yml配置文件处理类
 */
@Slf4j
public class ApplicationYmlUtil {

    private static final String[] files = {"application-code.yml", "application-req.yml", "application-regs.yml", "application-chain.yml"};
    private static long refreshInterval = 1000 * 60 * 3;
    public static long lastTime = System.currentTimeMillis();
    private static Map<String, String> contentMap = new HashMap<String, String>();

    static {
        try {
            load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>
     * 方法名称: load|描述:循环加载配置文件
     * </p>
     *
     * @throws Exception
     */
    private static void load() throws Exception {
        InputStream in = null;
        Yaml yaml = new Yaml();
        Map<String, String> map = null;
        for (String file : files) {
            log.info("加载配置文件filePath=" + ApplicationYmlUtil.class.getClassLoader().getResource(file));

            in = ApplicationYmlUtil.class.getClassLoader().getResourceAsStream(file);

            // 加载
            try {
                map = yaml.load(in);
                //load数据，key=数字，会作Integer类型put到map中，统一转换成字符串
                for (Object key : map.keySet()) {
                    if (null != key) {
                        contentMap.put(key + "", map.get(key));
                    }
                }
                in.close();
            } catch (IOException e) {
                log.error("加载配置文件异常", e);
            } finally {
                if (null != in) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        log.error("加载配置文件时，关闭输入流异常", e);
                    }
                    in = null;
                }
            }
        }
    }

    /**
     * <p>
     * 方法名称: reflesh|描述:刷新配置文件
     * </p>
     *
     * @throws Exception
     */
    private static void reflesh() throws Exception {
        long now = System.currentTimeMillis();
        if (now - lastTime > refreshInterval) {
            load();
            lastTime = now;
        }
    }

    public static String get(Integer key) {
        return get(key + "");
    }

    /**
     * <p>
     * 方法名称: getMessage|描述:根据键获取键值
     * </p>
     *
     * @param key
     * @return
     */
    public static String get(String key) {

        return null == contentMap.get(key) ? "" : contentMap.get(key).trim();
    }
}
