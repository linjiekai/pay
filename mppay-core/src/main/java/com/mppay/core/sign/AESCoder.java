package com.mppay.core.sign;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import com.mppay.core.constant.ConstEC;

public class AESCoder {

    // 密钥
    private static final String CRYPT_KEY = "y2W8CL6BkRrFlJPN";

    // 密钥偏移量IV
    private static final String IV_STRING = "dMbtHORyqseYwE0o";

    /**
     * 转换密钥
     *
     * @param key 密钥
     * @return 密钥
     */
    private static SecretKey toKey(String key) throws Exception {
        //实例化密钥材料
        SecretKey secretKey = new SecretKeySpec(key.getBytes(), ConstEC.AES_ALGORITHM);
        return secretKey;
    }

    /**
     * 指定密钥偏移量IV
     *
     * @param iv
     * @return
     * @throws Exception
     */
    private static IvParameterSpec toIv(String iv) throws Exception {
        // 指定密钥偏移量IV
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());
        return ivParameterSpec;
    }

    /**
     * 加密
     *
     * @param content 加密内容
     * @return 密文
     * @throws Exception
     */
    public static String encrypt(String content, String key, String iv) throws Exception {
        byte[] contentData = content.getBytes("UTF-8");
        // 为了与前端统一，这里的 key密钥 不可以使用 KeyGenerator、SecureRandom、SecretKey 生成
        SecretKey secretKey = toKey(key);
        // 指定加密的算法、工作模式和填充方式
        Cipher cipher = Cipher.getInstance(ConstEC.AES_CIPHER_ALGORITHM);
        if(StringUtils.isNotEmpty(iv)){
            //指定IV
            IvParameterSpec ivParameterSpec = toIv(iv);
            // 初始化，设置为加密模式
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
        }else {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        }
        //执行操作
        byte[] encryptedBytes = cipher.doFinal(contentData);
        //对加密后数据进行 base64 编码
        return Base64.encodeBase64String(encryptedBytes);
    }

    /**
     * 解密
     *
     * @param content 密文
     * @return 明文
     * @throws Exception
     */
    public static String decrypt(String content, String key, String iv) throws Exception {

        //转换密钥
        SecretKey secretKey = toKey(key);

        //base64解码，返回二进制编码
        byte[] contentData = Base64.decodeBase64(content);

        //指定IV
        IvParameterSpec ivParameterSpec = toIv(iv);

        // 指定加密的算法、工作模式和填充方式
        Cipher cipher = Cipher.getInstance(ConstEC.AES_CIPHER_ALGORITHM);
        //初始化，设置为解密模式
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
        //执行操作,返回解密后二进制编码
        byte[] result = cipher.doFinal(contentData);

        //二进制转换成字符串
        return new String(result, "UTF-8");
    }


    /**
     * 高汇通--AES加密
     *
     * @param plainBytes 明文字节数组
     * @param keyBytes   密钥字节数组
     * @param IV         随机向量
     * @return 加密后字节数组，不经base64编码
     * @throws Exception
     */
    public static byte[] encryptAESGHT(byte[] plainBytes, byte[] keyBytes, String IV) throws Exception {
        try {
            // AES密钥长度为128bit、192bit、256bit，默认为128bit
            if (keyBytes.length % 8 != 0 || keyBytes.length < 16 || keyBytes.length > 32) {
                throw new Exception("AES密钥长度不合法");
            }
            //加解密算法
            Cipher cipher = Cipher.getInstance(ConstEC.AES_CIPHER_ALGORITHM);
            SecretKey secretKey = new SecretKeySpec(keyBytes, ConstEC.AES_ALGORITHM);
            if (StringUtils.trimToNull(IV) != null) {
                IvParameterSpec ivspec = new IvParameterSpec(IV.getBytes());
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            }

            byte[] encryptedBytes = cipher.doFinal(plainBytes);
            return encryptedBytes;
        } catch (Exception e) {
            throw new Exception(e);

        }
    }
    
    public static void main(String[] args) throws Exception {
    	System.out.println(encrypt("oKyc01ayEasKYPFCD9OwLXuXXXZc", CRYPT_KEY, IV_STRING));
		System.out.println(decrypt("Exn8ZiI4bnPVumGXnF0zWXypmF4S5Xk5ht41pimDQK8=", CRYPT_KEY, IV_STRING));
	}
}