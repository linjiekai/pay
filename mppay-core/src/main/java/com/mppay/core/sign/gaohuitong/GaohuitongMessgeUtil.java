package com.mppay.core.sign.gaohuitong;

import com.mppay.core.exception.BusiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * 高汇通报文加密工具类
 */
@Slf4j
public class GaohuitongMessgeUtil {

    private static final String KEY_ALGORITHM_AES = "AES";
    private static final String CIPHER_ALGORITHM_AES = "AES/ECB/PKCS5Padding";
    private static final String CIPHER_ALGORITHM_RSA = "RSA/ECB/PKCS1Padding";
    private static final String IV = null;
    /**
     * 签名算法
     */
    private static final String SIGN_ALGORITHM = "SHA1WithRSA";
    /**
     * 密钥bit长度
     */
    private static final Integer KEY_LENGTH = 2048;
    /**
     * padding填充字节数，预留11字节
     */
    private static final Integer RESERVE_SIZE = 11;
    private static final String UNICODE_UTF_8 = "UTF-8";

    private static final String MESSAGE_ENCRYPT_DATA = "encryptData";
    private static final String MESSAGE_ENCRYPT_KEY = "encryptKey";
    private static final String MESSAGE_AGENCY_ID = "agencyId";
    private static final String MESSAGE_SIGN_DATA = "signData";
    private static final String MESSAGE_TRANCODE = "tranCode";
    private static final String MESSAGE_CALLBACK = "callBack";

    /**
     * @param :[filePath]
     * @return :java.security.PublicKey
     * @Description(描述): 获取公钥
     * @auther: Jack Lin
     * @date: 2019/9/7 10:52
     */
    public static PublicKey getPublicKey(String filePath) {
        return GaohuitongCryptoUtil.getRSAPublicKeyByFileSuffix(filePath, "pem", "RSA");
    }

    /**
     * @param :[filePath]
     * @return :java.security.PrivateKey
     * @Description(描述): 获取私钥
     * @auther: Jack Lin
     * @date: 2019/9/7 10:52
     */
    public static PrivateKey getPrivateKey(String filePath) {
        return GaohuitongCryptoUtil.getRSAPrivateKeyByFileSuffix(filePath, "pem", null, "RSA");
    }

    /**
     * 请求报文加密签名处理
     *
     * @param agencyId   机构ID
     * @param message    请求报文
     * @param key        报文加密 AES key
     * @param publicKey  RSA公钥
     * @param privateKey RSA私钥
     * @return dataMap
     * @throws Exception
     */
    public static Map<String, Object> requestHandle(String agencyId, String message, String key, PublicKey publicKey, PrivateKey privateKey, String tranCode, String callBack) throws Exception {
        Map<String, Object> msgMap = new HashMap<>();
        byte[] msgBytes = message.getBytes(UNICODE_UTF_8);
        byte[] keyBytes = key.getBytes(UNICODE_UTF_8);
        // AES报文加密
        String encryptData = encryptMessage(msgBytes, keyBytes);
        // 私钥加密签名
        String signData = digitalSign(msgBytes, privateKey, SIGN_ALGORITHM);
        // 公钥加密
        String encryptKey = encryptKey(keyBytes, publicKey, KEY_LENGTH, RESERVE_SIZE, CIPHER_ALGORITHM_RSA);
        // 报文封装
        msgMap.put(MESSAGE_ENCRYPT_DATA, encryptData);
        msgMap.put(MESSAGE_ENCRYPT_KEY, encryptKey);
        msgMap.put(MESSAGE_AGENCY_ID, agencyId);
        msgMap.put(MESSAGE_SIGN_DATA, signData);
        if (StringUtils.isNotBlank(tranCode)) {
            msgMap.put(MESSAGE_TRANCODE, tranCode);
        }
        if (StringUtils.isNotBlank(callBack)) {
            msgMap.put(MESSAGE_CALLBACK, callBack);
        }
        return msgMap;
    }

    /**
     * 响应报文解密处理
     *
     * @param message    响应报文
     * @param key        AES key
     * @param publicKey  RSA公钥
     * @param privateKey RSA私钥
     * @return
     * @throws Exception
     */
    public static String responseHandle(Map respMap,String key, PublicKey publicKey, PrivateKey privateKey) throws Exception {
        String encryptKey = (String) respMap.get(MESSAGE_ENCRYPT_KEY);
        String encryptData = (String) respMap.get(MESSAGE_ENCRYPT_DATA);
        String signData = (String) respMap.get(MESSAGE_SIGN_DATA);

        // 解密 encryptKey
        byte[] decryptKeyBytes = decryptKey(encryptKey, privateKey, KEY_LENGTH, RESERVE_SIZE, CIPHER_ALGORITHM_RSA);
        // 解密 encryptData
        byte[] messageBytes = decryptMessage(encryptData, decryptKeyBytes, KEY_ALGORITHM_AES, CIPHER_ALGORITHM_AES, IV);
        // 签名验证
        Boolean verifyFlag = verifyDigitalSign(messageBytes, signData, publicKey, SIGN_ALGORITHM);
        if (!verifyFlag) {
            log.error("GaohuitongMessgeUtil.responseHandle:签名验证失败");
            return null;
        }
        return new String(messageBytes, UNICODE_UTF_8);
    }

    /**
     * 请求报文-加密
     * 1. AES对称密钥
     * 2. Base64
     *
     * @param msgBytes 请求报文Bytes
     * @param keyBytes AES秘钥Bytes
     * @return
     * @throws Exception
     */
    private static String encryptMessage(byte[] msgBytes, byte[] keyBytes) {
        String encryptData;
        try {
            byte[] encryptDataBytes = GaohuitongCryptoUtil.AESEncrypt(msgBytes, keyBytes, KEY_ALGORITHM_AES, CIPHER_ALGORITHM_AES, IV);
            byte[] base64encryptDataBytes = Base64.encodeBase64(encryptDataBytes);
            encryptData = new String(base64encryptDataBytes, UNICODE_UTF_8);
        } catch (Exception e) {
            log.error("GaohuitongMessgeUtil.encryptMessage:{}", e);
            return null;
        }
        return encryptData;
    }

    /**
     * 响应报文-解密
     *
     * @return
     * @Param message 响应报文
     */
    private static byte[] decryptMessage(String encryptData, byte[] keyBytes, String keyAlgorithm, String cipherAlgorithm, String IV) {
        byte[] msgBytes;
        try {
            byte[] encryptDataBytes = Base64.decodeBase64(encryptData.getBytes(UNICODE_UTF_8));
            msgBytes = GaohuitongCryptoUtil.AESDecrypt(encryptDataBytes, keyBytes, keyAlgorithm, cipherAlgorithm, IV);
        } catch (Exception e) {
            log.error("GaohuitongMessgeUtil.decryptMessage:{}", e);
            return null;
        }
        return msgBytes;
    }

    /**
     * 请求报文 - encryptKey - 加密
     *
     * @param keyBytes        AES key 字节数组
     * @param publicKey       公钥
     * @param keyLength       密钥bit长度
     * @param reserveSize     padding填充字节数，预留11字节
     * @param cipherAlgorithm 加解密算法，一般为RSA/ECB/PKCS1Padding
     * @return
     */
    private static String encryptKey(byte[] keyBytes, PublicKey publicKey, int keyLength, int reserveSize, String cipherAlgorithm) {
        String encryptKey;
        try {
            byte[] base64EncryptKeyBytes = Base64.encodeBase64(GaohuitongCryptoUtil.RSAEncrypt(keyBytes, publicKey, keyLength, reserveSize, cipherAlgorithm));
            encryptKey = new String(base64EncryptKeyBytes, UNICODE_UTF_8);
        } catch (Exception e) {
            log.error("GaohuitongMessgeUtil.encryptKey:{}", e);
            return null;
        }
        return encryptKey;
    }

    /**
     * 响应报文 - encryptKey - 解密
     *
     * @param encryptKey      encryptKey
     * @param privateKey      私钥
     * @param keyLength       密钥bit长度
     * @param reserveSize     padding填充字节数，预留11字节
     * @param cipherAlgorithm 加解密算法，一般为RSA/ECB/PKCS1Padding
     * @return
     */
    private static byte[] decryptKey(String encryptKey, PrivateKey privateKey, int keyLength, int reserveSize, String cipherAlgorithm) {
        byte[] base64EncryptKeyBytes;
        try {
            byte[] encryptKeyBytes = Base64.decodeBase64(encryptKey.getBytes(UNICODE_UTF_8));
            base64EncryptKeyBytes = GaohuitongCryptoUtil.RSADecrypt(encryptKeyBytes, privateKey, keyLength, reserveSize, cipherAlgorithm);
        } catch (Exception e) {
            log.error("GaohuitongMessgeUtil.decryptKey:{}", e);
            return null;
        }
        return base64EncryptKeyBytes;
    }

    /**
     * 获取数字签名
     *
     * @param msgBytes      待签名明文字节数组
     * @param privateKey    签名使用私钥
     * @param signAlgorithm 签名算法
     * @return
     */
    private static String digitalSign(byte[] msgBytes, PrivateKey privateKey, String signAlgorithm) {
        String signData;
        try {
            byte[] base64signDataBytes = Base64.encodeBase64(GaohuitongCryptoUtil.digitalSign(msgBytes, privateKey, signAlgorithm));
            signData = new String(base64signDataBytes, UNICODE_UTF_8);
        } catch (Exception e) {
            log.error("GaohuitongMessgeUtil.digitalSign:{}", e);
            return null;
        }
        return signData;
    }

    /**
     * 验证签名
     *
     * @param msgBytes      响应报文 encryptData明文bytes
     * @param signData      响应报文 signData
     * @param publicKey     公钥
     * @param signAlgorithm 算法
     * @return verifyFlag
     */
    private static Boolean verifyDigitalSign(byte[] msgBytes, String signData, PublicKey publicKey, String signAlgorithm) {
        Boolean verifyFlag;
        try {
            byte[] signBytes = Base64.decodeBase64(signData.getBytes(UNICODE_UTF_8));
            verifyFlag = GaohuitongCryptoUtil.verifyDigitalSign(msgBytes, signBytes, publicKey, signAlgorithm);
        } catch (Exception e) {
            log.error("GaohuitongMessgeUtil.verifyDigitalSign:{}", e);
            return null;
        }
        return verifyFlag;
    }

    /**
     * 响应报文解析
     *
     * @param response
     * @return
     */
    public static Map parseMap(String response) {
        Map<String, String> map = new HashMap<>();
        String[] resp = response.split("&");
        for (int i = 0; i < resp.length; i++) {
            String key = resp[i].split("=")[0];
            String value = resp[i].split("=")[1];
            map.put(key, value);
        }
        return map;
    }

}
