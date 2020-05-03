package com.mppay.core.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.micrometer.core.instrument.util.StringUtils;
import org.dom4j.*;

import java.util.List;

public class XmlUtil {

    /**
     * xml转json
     *
     * @param xmlStr
     * @return
     * @throws DocumentException
     */
    public static JSONObject xml2Json(String xmlStr) throws DocumentException {
        Document doc = DocumentHelper.parseText(xmlStr);
        JSONObject json = new JSONObject();
        dom4j2Json(doc.getRootElement(), json);
        return json;
    }

    /**
     * xml转json
     *
     * @param element
     * @param json
     */
    private static void dom4j2Json(Element element, JSONObject json) {
        //如果是属性
        for (Object o : element.attributes()) {
            Attribute attr = (Attribute) o;
            if (StringUtils.isNotBlank(attr.getValue())) {
                json.put("@" + attr.getName(), attr.getValue());
            }
        }
        List<Element> chdEl = element.elements();
        //如果没有子元素,只有一个值
        if (chdEl.isEmpty() && StringUtils.isNotBlank(element.getText())) {
            json.put(element.getName(), element.getText());
        }
        //有子元素
        for (Element e : chdEl) {
            //子元素也有子元素
            if (!e.elements().isEmpty()) {
                JSONObject chdjson = new JSONObject();
                dom4j2Json(e, chdjson);
                Object o = json.get(e.getName());
                if (o != null) {
                    JSONArray jsona = null;
                    //如果此元素已存在,则转为jsonArray
                    if (o instanceof JSONObject) {
                        JSONObject jsono = (JSONObject) o;
                        json.remove(e.getName());
                        jsona = new JSONArray();
                        jsona.add(jsono);
                        jsona.add(chdjson);
                    }
                    if (o instanceof JSONArray) {
                        jsona = (JSONArray) o;
                        jsona.add(chdjson);
                    }
                    json.put(e.getName(), jsona);
                } else {
                    if (!chdjson.isEmpty()) {
                        json.put(e.getName(), chdjson);
                    }
                }
                //子元素没有子元素
            } else {
                for (Object o : element.attributes()) {
                    Attribute attr = (Attribute) o;
                    if (StringUtils.isNotBlank(attr.getValue())) {
                        json.put("@" + attr.getName(), attr.getValue());
                    }
                }
                if (!e.getText().isEmpty()) {
                    json.put(e.getName(), e.getText());
                }
            }
        }
    }
}
