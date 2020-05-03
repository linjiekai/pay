package com.mppay.core.utils;

import com.alibaba.fastjson.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;

public class YamlUtil {
    public static void main(String[] args) {
        System.out.println(get("application-dev.yml", "bbb.ddd.ee1"));
    }

    /**
     *
     * @param yml 文件名
     * @param key 格式:xxx.yyy
     * @return
     */
    public static String get(String yml, String key)  {

        Yaml yaml = new Yaml();
        InputStream in = null;
        try {
            in = YamlUtil.class.getClassLoader().getResourceAsStream(yml);
            if (in == null) {
                return null;
            }
            JSONObject jsonObject = yaml.loadAs(in, JSONObject.class);
            String[] split = key.split("\\.");
            if (split.length == 0) {
                return jsonObject.getString(key);
            } else {
                for (int i = 0; i < split.length - 1;i++) {
                    jsonObject = jsonObject.getJSONObject(split[i]);
                }
                if (jsonObject == null) {
                    return null;
                }
                return jsonObject.getString(split[split.length-1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
