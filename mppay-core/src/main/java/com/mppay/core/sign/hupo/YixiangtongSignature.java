/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.mppay.core.sign.hupo;

import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.Charsets;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author runzhi
 */
@Slf4j
public class YixiangtongSignature {


    /**
     * 生成签名
     *
     * @param inputValues
     * @param ticket
     * @return
     */
    public static String sign(List<String> inputValues, String ticket) {
        if (inputValues == null) {
            throw new NullPointerException("values is null");
        }

        List<String> values = new ArrayList<>(inputValues);
        values.add(ticket);

        // remove null
        values.removeAll(Collections.singleton(null));
        Collections.sort(values);

        StringBuilder sb = new StringBuilder();
        values.forEach(sb::append);
        try {
            MessageDigest md = MessageDigest.getInstance("sha1");
            md.update(sb.toString().getBytes("UTF-8"));
            return sha1Sign(sb);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String sha1Sign(StringBuilder sb) {
        String sign = Hashing.sha1().hashString(sb, Charsets.UTF_8).toString().toUpperCase();
        return sign;
    }

}
