package com.mppay.core.utils;

import com.mppay.core.constant.ConstEC;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSACoder {

    /**
     * 生成密钥对：密钥对中包含公钥和私钥
     *
     * @return 包含 RSA 公钥与私钥的 keyPair
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public static KeyPair getKeyPair() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");    // 获得RSA密钥对的生成器实例
        SecureRandom secureRandom = new SecureRandom(String.valueOf(System.currentTimeMillis()).getBytes("utf-8")); // 说的一个安全的随机数
        keyPairGenerator.initialize(2048, secureRandom);    // 这里可以是1024、2048 初始化一个密钥对
        KeyPair keyPair = keyPairGenerator.generateKeyPair();   // 获得密钥对
        return keyPair;
    }

    /**
     * 获取公钥 (并进行Base64编码，返回一个 Base64 编码后的字符串)
     *
     * @param keyPair
     * @return 返回一个 Base64 编码后的公钥字符串
     */
    public static String getPublicKey(KeyPair keyPair) {
        PublicKey publicKey = keyPair.getPublic();
        byte[] bytes = publicKey.getEncoded();
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 获取私钥(并进行Base64编码，返回一个 Base64 编码后的字符串)
     *
     * @param keyPair
     * @return 返回一个 Base64 编码后的私钥字符串
     */
    public static String getPrivateKey(KeyPair keyPair) {
        PrivateKey privateKey = keyPair.getPrivate();
        byte[] bytes = privateKey.getEncoded();
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 将Base64编码后的公钥转换成 PublicKey 对象
     *
     * @param pubStr
     * @return PublicKey
     */
    public static PublicKey string2PublicKey(String pubStr) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] bytes = Base64.getDecoder().decode(pubStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }

    /**
     * 将Base64编码后的私钥转换成 PrivateKey 对象
     *
     * @param priStr
     * @return PrivateKey
     */
    public static PrivateKey string2Privatekey(String priStr) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] bytes = Base64.getDecoder().decode(priStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }

    /**
     * 公钥加密
     *
     * @param content   待加密的内容 byte[]
     * @param publicKey 加密所需的公钥对象 PublicKey
     * @return 加密后的字节数组 byte[]
     */
    public static byte[] publicEncrytype(byte[] content, PublicKey publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] bytes = cipher.doFinal(content);
        return bytes;
    }

    /**
     * 私钥解密
     *
     * @param content    待解密的内容 byte[]
     * @param privateKey 解密需要的私钥对象 PrivateKey
     * @return 解密后的字节数组 byte[]
     */
    public static byte[] privateDecrypt(byte[] content, PrivateKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] bytes = cipher.doFinal(content);
        return bytes;
    }


    /**
     * 高汇通--RSA加密
     *
     * @param plainBytes      明文字节数组
     * @param publicKey       公钥
     * @param keyLength       密钥bit长度
     * @param reserveSize     padding填充字节数，预留11字节
     * @return 加密后字节数组，不经base64编码
     * @throws Exception
     */
    public static byte[] RSAEncrypt(byte[] plainBytes, PublicKey publicKey, int keyLength, int reserveSize) throws Exception {
        int keyByteSize = keyLength / 8; // 密钥字节数
        int encryptBlockSize = keyByteSize - reserveSize; // 加密块大小=密钥字节数-padding填充字节数
        int nBlock = plainBytes.length / encryptBlockSize;// 计算分段加密的block数，向上取整
        if ((plainBytes.length % encryptBlockSize) != 0) { // 余数非0，block数再加1
            nBlock += 1;
        }
        try {
            Cipher cipher = Cipher.getInstance(ConstEC.RSA_CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            // 输出buffer，大小为nBlock个keyByteSize
            ByteArrayOutputStream outbuf = new ByteArrayOutputStream(nBlock * keyByteSize);
            // 分段加密
            for (int offset = 0; offset < plainBytes.length; offset += encryptBlockSize) {
                int inputLen = plainBytes.length - offset;
                if (inputLen > encryptBlockSize) {
                    inputLen = encryptBlockSize;
                }

                // 得到分段加密结果
                byte[] encryptedBlock = cipher.doFinal(plainBytes, offset, inputLen);
                // 追加结果到输出buffer中
                outbuf.write(encryptedBlock);
            }

            outbuf.flush();
            outbuf.close();
            return outbuf.toByteArray();
        } catch (Exception e) {
            throw new Exception(e);
        }
    }
}