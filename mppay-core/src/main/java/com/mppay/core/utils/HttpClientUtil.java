package com.mppay.core.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.exception.BusiException;

import io.micrometer.core.instrument.util.IOUtils;
import org.springframework.util.CollectionUtils;

/**
 *
 * TODO https 请求处理
 * @author chenfeihang
 * @time 2016年3月31日上午10:25:23
 * @type_name HttpClientUtil
 *
 */
public class HttpClientUtil {
	private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

	private static int CONNECTIONREQUESTTIMEOUT = 15 * 1000; // 秒
	private static int CONNECTTIMEOUT = 15 * 1000; // 秒
	private static int SOCKETTIMEOUT = 20 * 1000; // 秒
	private static int WEIXINCONNECTTIMEOUT = 10* 1000; // 秒
	private static int READTIMEOUT = 30 * 1000; // 秒

	/**
	 * http的请求方式:POST
	 */
	public static final String HTTP_REQUESTMETHOD_POST = "POST";
	/**
	 * http的请求方式:GET
	 */
	public static final String HTTP_REQUESTMETHOD_GET = "GET";


	public static CloseableHttpClient createSSLClientDefault(boolean isHttps) {
		try {
			if (isHttps) {
				SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
					// 信任所有
					public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
						return true;
					}
				}).build();
				SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
				return HttpClients.custom().setSSLSocketFactory(sslsf).build();
			}
		} catch (Exception e) {
			logger.error("创建HttpClient失败", e);
			throw new BusiException("9001", "创建HttpClient失败", e);
		}
		return HttpClients.createDefault();
	}

	public static String httpGet(String url, Map<String, Object> requestMap) throws Exception {
		return httpGet(url, false, requestMap);
	}

	public static String httpGet(String url, boolean isHttps, Map<String, Object> requestMap) throws Exception {

		StringBuffer sBuffer = new StringBuffer("");

		if (null != requestMap && requestMap.size() > 0) {
			for (Map.Entry<String, Object> entry : requestMap.entrySet()) {
				sBuffer.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
			}
			sBuffer.deleteCharAt(sBuffer.lastIndexOf("&"));
		}

		logger.info("request entity body：[{}]", sBuffer.toString());
		url += "?" + sBuffer.toString();
		HttpRequestBase requestBase = new HttpGet(url);
		return sendRequest(requestBase, isHttps);
	}

	public static String httpPost(String url, Map<String, Object> requestMap) throws Exception {
		return httpPost(url, false, requestMap);
	}

	public static String httpPost(String url, boolean isHttps, Map<String, Object> requestMap) throws Exception {
		logger.info("url[{}], isHttps[{}]", url, isHttps);
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		if (null != requestMap && requestMap.size() > 0) {
			for (Map.Entry<String, Object> entry : requestMap.entrySet()) {
				formparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
			}
		}

		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, ConstEC.ENCODE_UTF8);

		HttpRequestBase requestBase = new HttpPost(url);
		((HttpPost) requestBase).setEntity(entity);

		logger.info("request entity body：[{}]", EntityUtils.toString(entity));
		return sendRequest(requestBase, isHttps);
	}

	private static String sendRequest(HttpRequestBase requestBase, boolean isHttps) throws Exception {
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		try {
			RequestConfig config = RequestConfig.custom().setConnectionRequestTimeout(CONNECTIONREQUESTTIMEOUT)
					.setConnectTimeout(CONNECTTIMEOUT).setSocketTimeout(SOCKETTIMEOUT).build();
			requestBase.setConfig(config);
			logger.info("request line: {}", requestBase.getRequestLine());
			httpClient = HttpClientUtil.createSSLClientDefault(isHttps);

			response = httpClient.execute(requestBase);

			int statusCode = response.getStatusLine().getStatusCode();
			if (HttpStatus.SC_OK != statusCode) {
				throw new BusiException("9990", "连接失败，返回状态[" + statusCode + "]");
			}
			String result = EntityUtils.toString(response.getEntity(), ConstEC.ENCODE_UTF8);

			logger.info("收到的响应报文[{}]", result);
			return result;
		} catch (IOException e) {
			logger.error("调用外部IO异常", e);
			throw new BusiException("9001", "IO异常");
		} catch (Exception e) {
			logger.error("交易失败!", e);
			throw new BusiException("9991", "交易失败");
		} finally {
			if (null != response) {
				response.close();
				response = null;
			}
			if (null != httpClient) {
				httpClient.close();
				httpClient = null;
			}
		}
	}

	/**
	 * TODO:调用接口 HTTPS POST 无须证书
	 * @param requestUrl
	 * @param requestMethod
	 * @param outputStr
	 * @return return:String
	 * @throws Exception
	 */
	public static String httpsRequest(String requestUrl, String requestMethod, String outputStr) throws Exception {
		return httpsRequest(null, requestUrl, requestMethod, outputStr);
	}

	/**
	 * TODO:调用接口 HTTPS POST 无须证书
	 * @param requestUrl
	 * @param requestMethod
	 * @param outputStr
	 * @return return:String
	 * @throws Exception
	 */
	public static String httpsRequest(Map<String, Object> headerMap, String requestUrl, String requestMethod, String outputStr) throws Exception {
		StringBuffer buffer = new StringBuffer("");
		InputStream inputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		HttpsURLConnection httpUrlConn = null;
		try {
			// 创建SSLContext对象，并使用我们指定的信任管理器初始化
			TrustManager[] tm = { new CertificateTrustManager() };
			SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tm, new java.security.SecureRandom());
			// 从上述SSLContext对象中得到SSLSocketFactory对象
			SSLSocketFactory ssf = sslContext.getSocketFactory();
			URL url = new URL(requestUrl);
			httpUrlConn = (HttpsURLConnection) url.openConnection();
			httpUrlConn.setConnectTimeout(WEIXINCONNECTTIMEOUT);
			httpUrlConn.setReadTimeout(READTIMEOUT);
			httpUrlConn.setSSLSocketFactory(ssf);
			httpUrlConn.setDoOutput(true);
			httpUrlConn.setDoInput(true);
			httpUrlConn.setUseCaches(false);

			if (null != headerMap && headerMap.size() > 0) {
				for (Map.Entry<String, Object> entry : headerMap.entrySet()) {
					httpUrlConn.setRequestProperty(entry.getKey(), entry.getValue().toString());
				}
			}

			// 设置请求方式（GET/POST）
			httpUrlConn.setRequestMethod(requestMethod);
			if ("GET".equalsIgnoreCase(requestMethod)) {
				httpUrlConn.connect();
			}
			if (null != outputStr) {
				// 当有数据需要提交时
				OutputStream outputStream = httpUrlConn.getOutputStream();
				// 注意编码格式，防止中文乱码
				outputStream.write(outputStr.getBytes("UTF-8"));
				outputStream.close();
			}
			// 将返回的输入流转换成字符串
			inputStream = httpUrlConn.getInputStream();
			inputStreamReader = new InputStreamReader(inputStream, "utf-8");
			bufferedReader = new BufferedReader(inputStreamReader);
			String str = null;
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}

		} catch (SocketTimeoutException e) {
			logger.error("连接超时", e);
			throw new BusiException(13108, e);
		} catch (ConnectException e) {
			logger.error("连接失败", e);
			throw new BusiException(13108, e);
		} catch (Exception e) {
			logger.error("请求接口异常", e);
			throw new BusiException(13109, e);
		} finally {
			try {
				if (null != bufferedReader) {
					bufferedReader.close();
				}
				if (null != inputStreamReader) {
					inputStreamReader.close();
				}
				if (null != inputStream) {
					inputStream.close();
				}
				if (null != httpUrlConn) {
					httpUrlConn.disconnect();
				}
			} catch (IOException e) {
				logger.error("关闭连接失败", e);
			}
		}
		return buffer.toString();
	}

	/**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url    发送请求的 URL
     * @param params 请求的参数集合
     * @return 远程资源的响应结果
     */
    @SuppressWarnings("unused")
    public static String sendPost(String url, Map<String, Object> params, Map<String, Object> headers) {
        OutputStreamWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
        try {
            URL realUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // POST方法
            conn.setRequestMethod("POST");
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            if (null != headers && headers.size() > 0) {
				for (Map.Entry<String, Object> entry : headers.entrySet()) {
					conn.setRequestProperty(entry.getKey(), entry.getValue().toString());
				}
			}
            conn.setConnectTimeout(CONNECTTIMEOUT);
            conn.setReadTimeout(READTIMEOUT);
            conn.connect();
            // 获取URLConnection对象对应的输出流
            out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            // 发送请求参数
            if (params != null) {
                StringBuilder param = new StringBuilder();
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    if (param.length() > 0) {
                        param.append("&");
                    }
                    param.append(entry.getKey());
                    param.append("=");
                    param.append(entry.getValue());
                }
                out.write(param.toString());
            }
            System.out.println(params.toString());
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"));

            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            logger.error("请求接口异常", e);
			throw new BusiException("99999", "请求接口异常", e);
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                throw new BusiException("99999", "关闭连接失败", ex);
            }
        }
        return result.toString();
    }

	/**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url    发送请求的 URL
     * @param params 请求的参数集合
     * @return 远程资源的响应结果
     */
    @SuppressWarnings("unused")
    public static String sendPostJson(String url, Map<String, Object> params, Map<String, Object> headers) {
        OutputStreamWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
        try {
            URL realUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // POST方法
            conn.setRequestMethod("POST");
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type","application/json" );
            if (null != headers && headers.size() > 0) {
				for (Map.Entry<String, Object> entry : headers.entrySet()) {
					conn.setRequestProperty(entry.getKey(), entry.getValue().toString());
				}
			}
            conn.setConnectTimeout(CONNECTTIMEOUT);
            conn.setReadTimeout(READTIMEOUT);
            conn.connect();
            // 获取URLConnection对象对应的输出流
            out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            // 发送请求参数
            if (params != null) {
                out.write(JSONObject.toJSONString(params));
            }
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"));

            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            logger.error("请求接口异常", e);
			throw new BusiException("99999", "请求接口异常", e);
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                throw new BusiException("99999", "关闭连接失败", ex);
            }
        }
        return result.toString();
    }
	/**
	 * TODO:调用接口 HTTPS POST 无须证书
	 * @param requestUrl
	 * @param requestMethod
	 * @param outputStr
	 * @return return:String
	 * @throws Exception
	 */
	public static byte[] httpsRequestRetByteArray(String requestUrl, String requestMethod, String outputStr) {
		InputStream inputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		HttpsURLConnection httpUrlConn = null;
		try {
			// 创建SSLContext对象，并使用我们指定的信任管理器初始化
			TrustManager[] tm = { new CertificateTrustManager() };
			SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tm, new java.security.SecureRandom());
			// 从上述SSLContext对象中得到SSLSocketFactory对象
			SSLSocketFactory ssf = sslContext.getSocketFactory();
			URL url = new URL(requestUrl);
			httpUrlConn = (HttpsURLConnection) url.openConnection();
			httpUrlConn.setSSLSocketFactory(ssf);
			httpUrlConn.setDoOutput(true);
			httpUrlConn.setDoInput(true);
			httpUrlConn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			httpUrlConn.setRequestMethod(requestMethod);
			if ("GET".equalsIgnoreCase(requestMethod))
				httpUrlConn.connect();
			if (null != outputStr) {
				// 当有数据需要提交时
				OutputStream outputStream = httpUrlConn.getOutputStream();
				// 注意编码格式，防止中文乱码
				outputStream.write(outputStr.getBytes("UTF-8"));
				outputStream.close();
			}
			// 将返回的输入流转换成字符串
			inputStream = httpUrlConn.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buff = new byte[1024];
			int n = 0;
			while ((n = inputStream.read(buff)) > 0) {
				baos.write(buff, 0, n);
			}
			return baos.toByteArray();
		} catch (ConnectException ce) {
			logger.error("连接失败", ce);
			throw new BusiException("99999", "连接失败", ce);
		} catch (Exception e) {
			logger.error("请求接口异常", e);
			throw new BusiException("99999", "请求接口异常", e);
		} finally {
			try {
				if (null != bufferedReader) {
					bufferedReader.close();
				}
				if (null != inputStreamReader) {
					inputStreamReader.close();
				}
				if (null != inputStream) {
					inputStream.close();
				}
				if (null != httpUrlConn) {
					httpUrlConn.disconnect();
				}
			} catch (IOException e) {
				logger.error("关闭连接失败", e);
				throw new BusiException("99999", "关闭连接失败", e);
			}
		}
	}

	/**
	 * @param requestUrl
	 * @param requestMethod
	 * @param outputStr
	 * @return return:String
	 * @throws Exception
	 */
	public static byte[] httpRequestRetByteArray(String requestUrl, String requestMethod, String outputStr) {
		InputStream inputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		HttpURLConnection httpUrlConn = null;
		try {
			URL url = new URL(requestUrl);
			httpUrlConn = (HttpURLConnection) url.openConnection();
			httpUrlConn.setDoOutput(true);
			httpUrlConn.setDoInput(true);
			httpUrlConn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			httpUrlConn.setRequestMethod(requestMethod);
			if ("GET".equalsIgnoreCase(requestMethod)) {
				httpUrlConn.connect();
			}
			if (null != outputStr) {
				// 当有数据需要提交时
				OutputStream outputStream = httpUrlConn.getOutputStream();
				// 注意编码格式，防止中文乱码
				outputStream.write(outputStr.getBytes("UTF-8"));
				outputStream.close();
			}
			// 将返回的输入流转换成字符串
			inputStream = httpUrlConn.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buff = new byte[1024];
			int n = 0;
			while ((n = inputStream.read(buff)) > 0) {
				baos.write(buff, 0, n);
			}
			return baos.toByteArray();
		} catch (ConnectException e) {
			logger.error("连接失败", e);
			throw new BusiException("99999", "连接失败", e);
		} catch (Exception e) {
			logger.error("连接失败", e);
			throw new BusiException("99999", "连接失败", e);
		} finally {
			try {
				if (null != bufferedReader) {
					bufferedReader.close();
				}
				if (null != inputStreamReader) {
					inputStreamReader.close();
				}
				if (null != inputStream) {
					inputStream.close();
				}
				if (null != httpUrlConn) {
					httpUrlConn.disconnect();
				}
			} catch (IOException e) {
				logger.error("关闭连接失败", e);
				throw new BusiException("99999", "关闭连接失败", e);
			}
		}
	}

	/**
	 * 使用证书进行双向验证的https post
	 * @param requestUrl 请求地址
	 * @param requestMethod 请求方方式
	 * @param outputStr 请求内容
	 * @param keyWord 证书密钥
	 * @param keyPath 证书路径
	 * @return 返回报文
	 * @throws Exception
	 */
	public static String httpsRequestCert(String requestUrl, String requestMethod, String outputStr, String keyWord,
			String keyPath) throws Exception {
		String responseContent = null;
		// 指定读取证书格式为PKCS12
		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		// 读取本机存放的PKCS12证书文件
		InputStream instream = null;
		CloseableHttpResponse response = null;
		CloseableHttpClient httpclient = null;
		try {
			logger.debug("传输进来的路径为:" + keyPath);
			instream=  HttpClientUtil.class.getClassLoader().getResourceAsStream(keyPath);
			// 指定PKCS12的密码(商户ID)
			keyStore.load(instream, keyWord.toCharArray());

			@SuppressWarnings("deprecation")
			SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, keyWord.toCharArray()).build();

			// 指定TLS版本
			@SuppressWarnings("deprecation")
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null,
					SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);

			// 设置httpclient的SSLSocketFactory
			httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

			HttpPost httpPost = new HttpPost(requestUrl);
			if (outputStr != null) {
				StringEntity entity = new StringEntity(outputStr, Consts.UTF_8);
				httpPost.setEntity(entity);
			}

			response = httpclient.execute(httpPost);
			responseContent = Utf8ResponseHandler.INSTANCE.handleResponse(response);
		} catch (Exception e) {
			logger.error("交易失败!", e);
			throw new BusiException("99999", "交易失败", e);
		} finally {
			if (null != instream) {
				instream.close();
			}

			if (null != response) {
				response.close();
			}
			if (null != httpclient) {
				httpclient.close();
			}
		}
		return responseContent;
	}


	public static String excuteHttpRequest(String requestURL, Map<String,InputStream> inputStreams, Map<String, Object> params, Map<String, String> headers) throws Exception {
		CloseableHttpClient httpClient = null;
		MultipartEntityBuilder builder = null;
		HttpPost post = null;
		CloseableHttpResponse response = null;

		String var29;
		try {
			if (requestURL.contains("https://")) {
				SslUtil.ignoreSsl();
			}

			String SO_TIMEOUT = "1000000";
			Integer soTimeout = Integer.parseInt(SO_TIMEOUT);
			String CONNECTION_TIMEOUT = "1000000";
			Integer connectionTimeout = Integer.parseInt(CONNECTION_TIMEOUT);
			RequestConfig defaultRequestConfig = RequestConfig.custom().setSocketTimeout(soTimeout).setConnectTimeout(connectionTimeout).setConnectionRequestTimeout(connectionTimeout).setStaleConnectionCheckEnabled(true).build();
			httpClient = HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).build();
			builder = MultipartEntityBuilder.create();
			Iterator var13;
			Map.Entry entry;

			if (inputStreams!=null) {
				Set<String> strings = inputStreams.keySet();
				for(String s : strings){
					InputStream inputStream = inputStreams.get(s);
					builder.addBinaryBody(s, inputStream, ContentType.DEFAULT_BINARY, s);
				}
			}
			var13 = params.entrySet().iterator();

			while(var13.hasNext()) {
				entry = (Map.Entry)var13.next();
				ContentType contentType = ContentType.create("text/plain", "UTF-8");
				StringBody stringBody = new StringBody((String)entry.getValue(), contentType);
				builder.addPart((String)entry.getKey(), stringBody);
			}

			HttpEntity multipart = builder.build();
			post = new HttpPost(requestURL);
			RequestConfig requestConfig = RequestConfig.copy(defaultRequestConfig).build();
			post.setConfig(requestConfig);
			post.setEntity(multipart);
			if (headers != null && headers.size() > 0) {
				Iterator var25 = headers.entrySet().iterator();

				while(var25.hasNext()) {
					Map.Entry<String, String> header = (Map.Entry)var25.next();
					post.setHeader((String)header.getKey(), (String)header.getValue());
				}
			}

			post.setHeader("Content-Type", multipart.getContentType().getValue());
			response = httpClient.execute(post);
			String result = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
			var29 = result;
		} catch (Exception var20) {
			throw var20;
		} finally {
			HttpClientUtils.closeQuietly(httpClient);
			HttpClientUtils.closeQuietly(response);
		}

		return var29;
	}
}
