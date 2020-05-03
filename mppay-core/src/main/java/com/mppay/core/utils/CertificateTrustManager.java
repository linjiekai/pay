package com.mppay.core.utils;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * 证书信任管理器（用于https请求） TODO
 * @author chenfeihang
 * @time 2016年3月22日上午11:23:42
 * @type_name WechatTrustManager
 *
 */

public class CertificateTrustManager implements X509TrustManager {

	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

	}

	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

	}

	public X509Certificate[] getAcceptedIssuers() {

		return null;

	}

}
