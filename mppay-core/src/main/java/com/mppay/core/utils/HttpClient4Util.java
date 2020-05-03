package com.mppay.core.utils;

import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

public class HttpClient4Util {
	private CloseableHttpClient httpclient;
	private RequestConfig requestConfig;
	private PoolingHttpClientConnectionManager connManager;

	/**
	 * 构造函数
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	public HttpClient4Util() throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext sslCtx = SSLContext.getInstance("TLS");
		X509TrustManager trustManager = new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
			}
		};
		sslCtx.init(null, new TrustManager[] { trustManager }, null);
		LayeredConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslCtx,
				SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.<ConnectionSocketFactory> create();
		ConnectionSocketFactory plainSocketFactory = new PlainConnectionSocketFactory();
		registryBuilder.register("http", plainSocketFactory);
		registryBuilder.register("https", sslSocketFactory);

		Registry<ConnectionSocketFactory> registry = registryBuilder.build();
		// 设置连接管理器
		connManager = new PoolingHttpClientConnectionManager(registry);
		// 指定cookie存储对象
		BasicCookieStore cookieStore = new BasicCookieStore();

		// 连接保持活跃策略
		ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy() {
			@Override
			public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
				// 获取'keep-alive'HTTP报文头
				HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
				while (it.hasNext()) {
					HeaderElement he = it.nextElement();
					String param = he.getName();
					String value = he.getValue();
					if (value != null && param.equalsIgnoreCase("timeout")) {
						try {
							return Long.parseLong(value) * 1000;
						} catch (NumberFormatException ignore) {
						}
					}
				}
				// 保持65秒活跃
				return 65 * 1000;
			}
		};

		httpclient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).setConnectionManager(connManager).setKeepAliveStrategy(myStrategy)
				.build();

		requestConfig = RequestConfig.custom().setConnectTimeout(70000).setSocketTimeout(70000).build();
	}

	/**
	 * 基本的Get请求
	 * 
	 * @param url
	 *            请求URL
	 * @param nameValuePairs
	 *            请求List<NameValuePair>查询参数
	 * @return
	 */
	public byte[] doGet(String url, List<NameValuePair> nameValuePairs) {
		CloseableHttpResponse response = null;
		HttpGet httpget = new HttpGet();
		try {
			URIBuilder builder = new URIBuilder(url);
			// 填入查询参数
			if (nameValuePairs != null && !nameValuePairs.isEmpty()) {
				builder.setParameters(nameValuePairs);
			}
			httpget.setURI(builder.build());
			httpget.setConfig(requestConfig);
			response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				return EntityUtils.toByteArray(entity);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	
	/**
	 * 
	 * @param url
	 * @param queryParams
	 * @param formParams
	 * @return
	 */
	public byte[] doPost(String url, String param) {
		CloseableHttpResponse response = null;
		HttpPost httppost = new HttpPost();
//		httppost.addHeader("Content-Type", "text/html;charset=UTF-8");
		try {
			URIBuilder builder = new URIBuilder(url);
			// 填入查询参数
			/*if (queryParams != null && !queryParams.isEmpty()) {
				builder.setParameters(queryParams);
			}*/
			httppost.setURI(builder.build());
			httppost.setConfig(requestConfig);

			/*if (formParams != null && !formParams.isEmpty()) {
				httppost.setEntity(new UrlEncodedFormEntity(formParams));
			}
			if (byteEntity != null ) {
				httppost.setEntity(new ByteArrayEntity(byteEntity));
			}*/
			httppost.setEntity(new StringEntity(param, "UTF-8"));
			response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				return EntityUtils.toByteArray(entity);
			}
		} catch (Exception e) {
			System.out.println("11111");
			e.printStackTrace();
		} finally {
			httppost.releaseConnection();
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	

	/**
	 * 
	 * @param url
	 * @param queryParams
	 * @param formParams
	 * @return
	 */
	public byte[] doPost(String url, List<NameValuePair> queryParams, List<NameValuePair> formParams, byte[] byteEntity) {
		CloseableHttpResponse response = null;
		HttpPost httppost = new HttpPost();
//		httppost.addHeader("Content-Type", "text/html;charset=UTF-8");
		try {
			URIBuilder builder = new URIBuilder(url);
			// 填入查询参数
			if (queryParams != null && !queryParams.isEmpty()) {
				builder.setParameters(queryParams);
			}
			httppost.setURI(builder.build());
			httppost.setConfig(requestConfig);

			if (formParams != null && !formParams.isEmpty()) {
				httppost.setEntity(new UrlEncodedFormEntity(formParams));
			}
			if (byteEntity != null ) {
				httppost.setEntity(new ByteArrayEntity(byteEntity));
			}
			response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				return EntityUtils.toByteArray(entity);
			}
		} catch (Exception e) {
			System.out.println("11111");
			e.printStackTrace();
		} finally {
			httppost.releaseConnection();
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public void shutdown() throws IOException {
		connManager.shutdown();
		httpclient.close();
		synchronized (this) {
			notifyAll();
		}
	}
}