package com.mppay;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"com.mppay"})
@EnableTransactionManagement
@MapperScan("com.mppay.service.mapper")
@EnableScheduling
@EnableCaching
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.mppay.client.*")
public class MppayGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(MppayGatewayApplication.class, args);
    }
}
