package com.mppay.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages = {"com.mppay.service", "com.mppay.core", "com.mppay.admin"})
@ServletComponentScan(basePackages = {"com.mppay.admin.handler.auth"})
public class MppayAdminApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(MppayAdminApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(MppayAdminApplication.class, args);
	}

}
