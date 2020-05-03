package com.mppay.gateway.handler.utils;


import com.mppay.core.constant.BankCode;
import com.mppay.core.constant.SysCnlType;
import com.mppay.core.constant.joinpay.JoinpayConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class JoinpayUtil {

    public static String createLinkString(Map<String, Object> map, String merchantKey) {
        List<String> keys = new ArrayList<String>(map.keySet());
        Collections.sort(keys);
        String str1 = "";
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            Object value = map.get(key);//(String) 强制类型转换
            if (value == null) {
                continue;
            }
            if (i == keys.size() - 1) {
                str1 = str1 + value.toString();
            } else {
                str1 = str1 + value;
            }
        }
        log.info("|joinpay|签名串：{}", str1);
        return SignByMD5(str1, merchantKey);
    }

    public static String SignByMD5(String requestSign, String merchantKey) {
        String reqHmac = "";
        try {
            reqHmac = DigestUtils.md5Hex(requestSign + merchantKey).toUpperCase();
        } catch (Exception e) {

        }
        return reqHmac;
    }


    public static String queryFrpcode(String bankCode, String sysCnl) {
        if (BankCode.WEIXIN.getId().equalsIgnoreCase(bankCode)) {
            switch (SysCnlType.parse(sysCnl)) {
                case WX_APPLET:
                    return JoinpayConstants.FRPCODE_WEIXIN_XCX;
                case WX_PUBLIC:
                    return JoinpayConstants.FRPCODE_WEIXIN_GZH;
                case H5:
                    return JoinpayConstants.FRPCODE_WEIXIN_H5;
                case IOS:
                    return JoinpayConstants.FRPCODE_WEIXIN_APP;
                case ANDROID:
                    return JoinpayConstants.FRPCODE_WEIXIN_APP;
            }
        }
        return "";
    }


    public static String getPlain(Map<String, Object> requestMap) throws Exception {
        StringBuffer plain = new StringBuffer("");
        List<String> keys = new ArrayList<String>(requestMap.keySet());
        Collections.sort(keys);
        Object value = null;
        for (String key : keys) {
            value = requestMap.get(key.trim());
            //值为空的字段不参与签名
            if (null != value && !"".equals(value.toString().trim()) && !"sign".equals(key.toString().trim())
                    && (!(value instanceof List) && !(value instanceof Map))) {
                plain.append(key + "=" + (String) URLEncoder.encode(value.toString(), "utf-8") + "&");
            }
        }
        //去掉最后一个&符号
        if (plain.length() > 0 && plain.lastIndexOf("&") >= 0) {
            plain.deleteCharAt(plain.lastIndexOf("&"));
        }
        return plain.toString();
    }

}
