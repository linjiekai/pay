package com.mppay.core.sign.gaohuitong;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Title : GaohuitongCryptoUtil
 * Description : 高汇通加解密工具类
 */
@Slf4j
public abstract class GaohuitongCryptoUtil {

    /**
     * 数字签名函数入口
     *
     * @param plainBytes    待签名明文字节数组
     * @param privateKey    签名使用私钥
     * @param signAlgorithm 签名算法
     * @return 签名后的字节数组
     * @throws Exception
     */
    public static byte[] digitalSign(byte[] plainBytes, PrivateKey privateKey, String signAlgorithm) throws Exception {
        try {
            Signature signature = Signature.getInstance(signAlgorithm);
            signature.initSign(privateKey);
            signature.update(plainBytes);
            byte[] signBytes = signature.sign();
            return signBytes;
        } catch (NoSuchAlgorithmException e) {
            throw new Exception(e);
        } catch (InvalidKeyException e) {
            throw new Exception(e);
        } catch (SignatureException e) {
            throw new Exception(e);
        }
    }

    /**
     * 验证数字签名函数入口
     *
     * @param plainBytes    待验签明文字节数组
     * @param signBytes     待验签签名后字节数组
     * @param publicKey     验签使用公钥
     * @param signAlgorithm 签名算法
     * @return 验签是否通过
     * @throws Exception
     */
    public static boolean verifyDigitalSign(byte[] plainBytes, byte[] signBytes, PublicKey publicKey, String signAlgorithm) throws Exception {
        boolean isValid = false;
        try {
            Signature signature = Signature.getInstance(signAlgorithm);
            signature.initVerify(publicKey);
            signature.update(plainBytes);
            isValid = signature.verify(signBytes);
            return isValid;
        } catch (NoSuchAlgorithmException e) {
            throw new Exception(e);
        } catch (InvalidKeyException e) {
            throw new Exception(e);
        } catch (SignatureException e) {
            throw new Exception(e);
        }
    }

    /**
     * 获取RSA公钥对象
     *
     * @param filePath     RSA公钥路径
     * @param fileSuffix   RSA公钥名称
     * @param keyAlgorithm 密钥算法
     * @return RSA公钥对象
     * @throws Exception
     */
    public static PublicKey getRSAPublicKeyByFileSuffix(String filePath, String fileSuffix, String keyAlgorithm) {
        InputStream in = null;
        try {
            in = GaohuitongCryptoUtil.class.getClassLoader().getResourceAsStream(filePath);
            PublicKey pubKey = null;
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String readLine = null;
            while ((readLine = br.readLine()) != null) {
                if (readLine.charAt(0) == '-') {
                    continue;
                } else {
                    sb.append(readLine);
                    sb.append('\r');
                }
            }
            X509EncodedKeySpec pubX509 = new X509EncodedKeySpec(Base64.decodeBase64(sb.toString()));
            KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
            pubKey = keyFactory.generatePublic(pubX509);
            return pubKey;
        } catch (Exception e) {
            log.error("getRSAPublicKeyByFileSuffix error:{}", e);
            return null;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {

            }
        }
    }

    /**
     * 获取RSA私钥对象
     *
     * @param filePath     RSA私钥路径
     * @param fileSuffix   RSA私钥名称
     * @param password     RSA私钥保护密钥
     * @param keyAlgorithm 密钥算法
     * @return RSA私钥对象
     * @throws Exception
     */
    @SuppressWarnings("deprecation")
    public static PrivateKey getRSAPrivateKeyByFileSuffix(String filePath, String fileSuffix, String password, String keyAlgorithm) {
        InputStream in = null;
        try {
            in = GaohuitongCryptoUtil.class.getClassLoader().getResourceAsStream(filePath);
            PrivateKey priKey = null;
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String readLine = null;
            while ((readLine = br.readLine()) != null) {
                if (readLine.charAt(0) == '-') {
                    continue;
                } else {
                    sb.append(readLine);
                    sb.append('\r');
                }
            }
            PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.decodeBase64(sb.toString()));
            KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
            priKey = keyFactory.generatePrivate(priPKCS8);
            return priKey;
        } catch (Exception e) {
            log.error("getRSAPrivateKeyByFileSuffix error:{}", e);
            return null;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * RSA加密
     *
     * @param keyBytes        AES key字节数组
     * @param publicKey       公钥
     * @param keyLength       密钥bit长度
     * @param reserveSize     padding填充字节数，预留11字节
     * @param cipherAlgorithm 加解密算法，一般为RSA/ECB/PKCS1Padding
     * @return 加密后字节数组，不经base64编码
     * @throws Exception
     */
    public static byte[] RSAEncrypt(byte[] keyBytes, PublicKey publicKey, int keyLength, int reserveSize, String cipherAlgorithm) throws Exception {
        // 密钥字节数
        int keyByteSize = keyLength / 8;
        // 加密块大小=密钥字节数-padding填充字节数
        int encryptBlockSize = keyByteSize - reserveSize;
        // 计算分段加密的block数，向上取整
        int nBlock = keyBytes.length / encryptBlockSize;
        // 余数非0，block数再加1
        if ((keyBytes.length % encryptBlockSize) != 0) {
            nBlock += 1;
        }
        try {
            Cipher cipher = Cipher.getInstance(cipherAlgorithm);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            // 输出buffer，大小为nBlock个keyByteSize
            ByteArrayOutputStream outbuf = new ByteArrayOutputStream(nBlock * keyByteSize);
            // 分段加密
            for (int offset = 0; offset < keyBytes.length; offset += encryptBlockSize) {
                int inputLen = keyBytes.length - offset;
                if (inputLen > encryptBlockSize) {
                    inputLen = encryptBlockSize;
                }

                // 得到分段加密结果
                byte[] encryptedBlock = cipher.doFinal(keyBytes, offset, inputLen);
                // 追加结果到输出buffer中
                outbuf.write(encryptedBlock);
            }
            outbuf.flush();
            outbuf.close();
            return outbuf.toByteArray();
        } catch (NoSuchAlgorithmException e) {
            throw new Exception(e);
        } catch (NoSuchPaddingException e) {
            throw new Exception(e);
        } catch (InvalidKeyException e) {
            throw new Exception(e);
        } catch (IllegalBlockSizeException e) {
            throw new Exception(e);
        } catch (BadPaddingException e) {
            throw new Exception(e);
        } catch (IOException e) {
            throw new Exception(e);
        }
    }

    /**
     * RSA解密
     *
     * @param encryptedBytes  加密后字节数组
     * @param privateKey      私钥
     * @param keyLength       密钥bit长度
     * @param reserveSize     padding填充字节数，预留11字节
     * @param cipherAlgorithm 加解密算法，一般为RSA/ECB/PKCS1Padding
     * @return 解密后字节数组，不经base64编码
     * @throws Exception
     */
    public static byte[] RSADecrypt(byte[] encryptedBytes, PrivateKey privateKey, int keyLength, int reserveSize, String cipherAlgorithm)
            throws Exception {
        int keyByteSize = keyLength / 8; // 密钥字节数
        int decryptBlockSize = keyByteSize - reserveSize; // 解密块大小=密钥字节数-padding填充字节数
        int nBlock = encryptedBytes.length / keyByteSize;// 计算分段解密的block数，理论上能整除
        try {
            Cipher cipher = Cipher.getInstance(cipherAlgorithm);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            // 输出buffer，大小为nBlock个decryptBlockSize
            ByteArrayOutputStream outbuf = new ByteArrayOutputStream(nBlock * decryptBlockSize);
            // 分段解密
            for (int offset = 0; offset < encryptedBytes.length; offset += keyByteSize) {
                // block大小: decryptBlock 或 剩余字节数
                int inputLen = encryptedBytes.length - offset;
                if (inputLen > keyByteSize) {
                    inputLen = keyByteSize;
                }

                // 得到分段解密结果
                byte[] decryptedBlock = cipher.doFinal(encryptedBytes, offset, inputLen);
                // 追加结果到输出buffer中
                outbuf.write(decryptedBlock);
            }
            outbuf.flush();
            outbuf.close();
            return outbuf.toByteArray();
        } catch (NoSuchAlgorithmException e) {
            throw new Exception(e);
        } catch (NoSuchPaddingException e) {
            throw new Exception(e);
        } catch (InvalidKeyException e) {
            throw new Exception(e);
        } catch (IllegalBlockSizeException e) {
            throw new Exception(e);
        } catch (BadPaddingException e) {
            throw new Exception(e);
        } catch (IOException e) {
            throw new Exception(e);
        }
    }

    /**
     * AES加密
     *
     * @param plainBytes      明文字节数组
     * @param keyBytes        密钥字节数组
     * @param keyAlgorithm    密钥算法
     * @param cipherAlgorithm 加解密算法
     * @param IV              随机向量
     * @return 加密后字节数组，不经base64编码
     * @throws Exception
     */
    public static byte[] AESEncrypt(byte[] plainBytes, byte[] keyBytes, String keyAlgorithm, String cipherAlgorithm, String IV) throws Exception {
        try {
            // AES密钥长度为128bit、192bit、256bit，默认为128bit
            if (keyBytes.length % 8 != 0 || keyBytes.length < 16 || keyBytes.length > 32) {
                throw new Exception("AES密钥长度不合法");
            }
            Cipher cipher = Cipher.getInstance(cipherAlgorithm);
            SecretKey secretKey = new SecretKeySpec(keyBytes, keyAlgorithm);
            if (StringUtils.isNotBlank(IV)) {
                IvParameterSpec ivspec = new IvParameterSpec(IV.getBytes());
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            }
            byte[] encryptedBytes = cipher.doFinal(plainBytes);
            return encryptedBytes;
        } catch (NoSuchAlgorithmException e) {
            throw new Exception(e);
        } catch (NoSuchPaddingException e) {
            throw new Exception(e);
        } catch (InvalidKeyException e) {
            throw new Exception(e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new Exception(e);
        } catch (BadPaddingException e) {
            throw new Exception(e);
        } catch (IllegalBlockSizeException e) {
            throw new Exception(e);
        }
    }

    /**
     * AES解密
     *
     * @param encryptedBytes  密文字节数组，不经base64编码
     * @param keyBytes        密钥字节数组
     * @param keyAlgorithm    密钥算法
     * @param cipherAlgorithm 加解密算法
     * @param IV              随机向量
     * @return 解密后字节数组
     * @throws Exception
     */
    public static byte[] AESDecrypt(byte[] encryptedBytes, byte[] keyBytes, String keyAlgorithm, String cipherAlgorithm, String IV) throws Exception {
        try {
            // AES密钥长度为128bit、192bit、256bit，默认为128bit
            if (keyBytes.length % 8 != 0 || keyBytes.length < 16 || keyBytes.length > 32) {
                throw new Exception("AES密钥长度不合法");
            }
            Cipher cipher = Cipher.getInstance(cipherAlgorithm);
            SecretKey secretKey = new SecretKeySpec(keyBytes, keyAlgorithm);
            if (IV != null && StringUtils.isNotBlank(IV)) {
                IvParameterSpec ivspec = new IvParameterSpec(IV.getBytes());
                cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
            }
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return decryptedBytes;
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("NoSuchAlgorithmException");
        } catch (NoSuchPaddingException e) {
            throw new Exception("NoSuchPaddingException");
        } catch (InvalidKeyException e) {
            throw new Exception("InvalidKeyException");
        } catch (InvalidAlgorithmParameterException e) {
            throw new Exception("InvalidAlgorithmParameterException");
        } catch (BadPaddingException e) {
            throw new Exception("BadPaddingException");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("IllegalBlockSizeException");
        }
    }


}
