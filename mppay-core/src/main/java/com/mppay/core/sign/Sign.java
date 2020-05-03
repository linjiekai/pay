package com.mppay.core.sign;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import com.mppay.core.utils.HexStr;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Sign {
    private static  final String signAlgorithm="SHA1WithRSA";
    //生成待签名串
    public static String getPlain(Map<String, Object> requestMap, String[] keys) throws Exception {
        StringBuffer plain = new StringBuffer("");

        //数组按KEY排序
        Arrays.sort(keys);

        Object value = null;
        //循环拼plain字符key1=value1&key2=value2&key3=value3…
        for (String key : keys) {
            value = requestMap.get(key.trim());
            //值为空的字段不参与签名
            if (null != value && !"".equals(value.toString().trim()) && !"sign".equals(key.toString().trim())
                    && !(value instanceof List) && !(value instanceof Map)) {
                plain.append(key + "=" + value.toString().trim() + "&");
            }
        }
        //去掉最后一个&符号
        if (plain.length() > 0 && plain.lastIndexOf("&") >= 0) {
            plain.deleteCharAt(plain.lastIndexOf("&"));
        }
        return plain.toString();
    }

    //生成待签名串
    public static String getPlain(Map<String, Object> requestMap) throws Exception {
        return getPlain(requestMap, false);
    }

    /**
     * 生成待签名串
     *
     * @param requestMap
     * @param composite  false去掉复合类型  true保留复合类型
     * @return
     * @throws Exception
     */
    public static String getPlain(Map<String, Object> requestMap, boolean composite) throws Exception {
        StringBuffer plain = new StringBuffer("");

        List<String> keys = new ArrayList<String>(requestMap.keySet());
        Collections.sort(keys);

        Object value = null;

        //循环拼plain字符key1=value1&key2=value2&key3=value3…
        for (String key : keys) {
            value = requestMap.get(key.trim());
            //值为空的字段不参与签名
            if (null != value && !"".equals(value.toString().trim()) && !"sign".equals(key.toString().trim())
                    && (composite || (!(value instanceof List) && !(value instanceof Map)))
            ) {
                plain.append(key + "=" + value.toString().trim() + "&");
            }
        }

        //去掉最后一个&符号
        if (plain.length() > 0 && plain.lastIndexOf("&") >= 0) {
            plain.deleteCharAt(plain.lastIndexOf("&"));
        }
        return plain.toString();
    }

    public static String getPlainURLEncoder(Map<String, Object> requestMap, String charset) throws IOException {
        StringBuffer plain = new StringBuffer("");

        List<String> keys = new ArrayList<String>(requestMap.keySet());
        Collections.sort(keys);

        Object value = null;

        //循环拼plain字符key1=value1&key2=value2&key3=value3…
        for (String key : keys) {
            value = requestMap.get(key.trim());
            //值为空的字段不参与签名
            if (null != value && !"".equals(value.toString().trim()) && !"biz_content".equals(key.toString().trim())
                    && !(value instanceof List) && !(value instanceof Map)
            ) {
                plain.append(key + "=" + URLEncoder.encode(value.toString().trim(), charset) + "&");
            }
        }

        //去掉最后一个&符号
        if (plain.length() > 0 && plain.lastIndexOf("&") >= 0) {
            plain.deleteCharAt(plain.lastIndexOf("&"));
        }
        return plain.toString();

    }

    //签名
    public static String sign(String plain) throws Exception {
        return Base64.encodeBase64String(signToHex(plain).getBytes());
    }

    //签名
    public static String signToHex(String plain) throws Exception {

        byte[] data;
        data = MD5Sign.encode(plain.getBytes("UTF-8"));
        return HexStr.bytesToHexString(data);
    }
    
    //签名
    public static String signSHA256ToHex(String plain) throws Exception {

        byte[] data;
        data = SHACoder.encode256(plain.getBytes("UTF-8"));
        return HexStr.bytesToHexString(data);
    }

    //验证签名
    public static boolean verify(String plain, String sign) throws Exception {
        String sign1 = sign(plain);
        log.info("signFaild：{}，signSuccess：{}",sign,sign1);
        if (sign.equalsIgnoreCase(sign(plain))) {
            return true;
        }

        return false;
    }

    //验证签名
    public static boolean verifyToHex(String plain, String sign) throws Exception {
        if (sign.equalsIgnoreCase(signToHex(plain))) {
            return true;
        }

        return false;
    }
    
  //验证签名
    public static boolean verifySHA256ToHex(String plain, String sign) throws Exception {
    	System.out.println(signSHA256ToHex(plain));
        if (sign.equalsIgnoreCase(signSHA256ToHex(plain))) {
            return true;
        }

        return false;
    }



    /**
     * 高汇通--数字签名函数入口
     *
     * @param plainBytes    待签名明文字节数组
     * @param privateKey    签名使用私钥
     * @param signAlgorithm 签名算法
     * @return 签名后的字节数组
     * @throws Exception
     */
    public static byte[] digitalSign(byte[] plainBytes, PrivateKey privateKey) throws Exception {
        try {
            Signature signature = Signature.getInstance(signAlgorithm);
            signature.initSign(privateKey);
            signature.update(plainBytes);
            byte[] signBytes = signature.sign();

            return signBytes;
        } catch (Exception e) {
            throw new Exception(e);
        }
    }
    
    public static void main(String[] args) throws Exception {
    	String plain = "X-MPMALL-SignVer=v1&busiType=02&clientIp=0.0.0.0&mercId=888000000000004&methodType=DirectPrePay&mobile=13119656021&notifyUrl=https://test-zb-api.zhuanbo.gdxfhl.com/shop/mobile/pay/notify&orderDate=2020-04-26&orderNo=2020042600231160&orderTime=18:09:08&period=1&periodUnit=02&platform=ZBMALL&price=36.00&requestId=1587895748477&sysCnl=WX-APPLET&tradeCode=02&userId=11182&key=12345678";
    	
		System.out.println(sign(plain));
	}
}
