package com.mppay.core.utils;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "redisson")
@Data
public class RedissonProperties {

	private String address;
	private String password;
	// 毫秒
	private int timeout = 30000;
	private int database = 0;
	private String sentinelAddress;
	private int connectionPoolSize = 20;
	private int connectionMiniumIdleSize = 10;
	private int slaveConnectionPoolSize = 30;
	private int masterConnectionPoolSize = 30;
	private String[] sentinelAddresses;
	private String[] masterAddresses;
	// 毫秒
	private int scanInterval = 2000;
	private String masterName;

}
