package phitrading.phitradingexchangemain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.phitrading.exchange.integration")
@ComponentScan({"phitrading.phitradingexchangemain", "com.phitrading.exchange"})
@EntityScan(basePackages = {"com.phitrading.exchange.domain.entity"})
@EnableJpaRepositories(basePackages = {"com.phitrading.exchange.domain.repository"})
public class PhiTradingExchangeMainApplication {

	public static void main(String[] args) {
		SpringApplication.run(PhiTradingExchangeMainApplication.class, args);
	}

}
