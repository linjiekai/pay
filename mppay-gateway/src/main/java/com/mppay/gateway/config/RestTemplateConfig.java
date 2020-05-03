package com.mppay.gateway.config;

import com.alibaba.cloud.sentinel.annotation.SentinelRestTemplate;
import lombok.Data;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConfigurationProperties(prefix = "spring.resttemplate")
@Data
public class RestTemplateConfig {
	private Integer maxConnectionTotal;
	private Integer defaultMaxPerRoute;
	private Integer connectTimeout;
	private Integer readTimeout;

	@Bean
	@SentinelRestTemplate
	RestTemplate restTemplate() {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		// Increase max total connection
		cm.setMaxTotal(maxConnectionTotal);
		// Increase default max connection per route
		cm.setDefaultMaxPerRoute(defaultMaxPerRoute);

		HttpClientBuilder httpClientBuilder = HttpClients.custom();
		RequestConfig.Builder builder = RequestConfig.custom();
		builder.setSocketTimeout(20000);

		RequestConfig requestConfig = builder.build();
		httpClientBuilder.setDefaultRequestConfig(requestConfig);
		HttpClient httpClient = httpClientBuilder.setConnectionManager(cm).build();
		HttpComponentsClientHttpRequestFactory httpFactory = new HttpComponentsClientHttpRequestFactory(
				httpClient);
		httpFactory.setConnectTimeout(connectTimeout);
		httpFactory.setReadTimeout(readTimeout);
		RestTemplate rest = new RestTemplate(httpFactory);
		return rest;
	}
}
