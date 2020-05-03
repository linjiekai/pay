package com.mppay.core.sign.joinpay;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @title: JoinpaySignature
 * @description: Joinpay签名处理
 * @date 2020/4/21 15:15
 */
public class JoinpaySignature {

    private static final String HMAC = "hmac";

    /**
     * 生成MD5签名
     *
     * @param textMap
     * @param signKey
     * @return
     */
    public static String getSignByMD5(Map<String, Object> textMap, String signKey) {
        String signText = getSignTextByMap(textMap);
        signText += signKey;
        return DigestUtils.md5Hex(signText).toUpperCase();
    }

    /**
     * MD5签名验证
     *
     * @param textMap
     * @param signKey
     * @return
     */
    public static boolean checkSignByMD5(Map<String, Object> textMap, String signKey) {
        String hmac = (String) textMap.get(HMAC);
        textMap.remove(HMAC);
        String sign = getSignByMD5(textMap, signKey);
        return hmac.equals(sign);
    }

    /**
     * 生成RSA签名
     *
     * @param textMap
     * @param signKey
     * @return
     */
    public static String getSignByRSA(Map<String, Object> textMap, String signKey) {
        return null;
    }

    /**
     * 拼接验签文本 - 根据Map
     *
     * @param params Map
     * @return
     */
    public static String getSignTextByMap(Map<String, Object> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        StringBuffer textStr = new StringBuffer();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            Object value = params.get(key);
            textStr.append(value.toString());
        }
        return textStr.toString();
    }

//    public static void main(String[] args) throws Exception {
//        JoinpayQueryRefundReq jp = new JoinpayQueryRefundReq();
//        jp.setP1_MerchantNo("MERCHANTNO");
//        jp.setP2_RefundOrderNo("REFUNDORDER");
//        jp.setP3_Version(123);
//        String signTextByObj = JoinpaySignature.getSignTextByObj(jp);
//        System.out.println(signTextByObj);
//        Map paramMap = JSONObject.parseObject(JSONObject.toJSONString(jp), Map.class);
//        String s = JoinpaySignature.getSignByMD5(paramMap, "123");
//        System.out.println(s);
//    }
}

