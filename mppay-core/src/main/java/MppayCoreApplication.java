

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication

@EnableTransactionManagement
@MapperScan("com.mppay.service.mapper")
@EnableScheduling
@EnableCaching

public class MppayCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(MppayCoreApplication.class, args);
	}

}
