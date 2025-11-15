package phitrading.phitradingexchangemain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"phitrading.phitradingexchangemain", "com.phitrading.exchange"})
@EnableFeignClients(basePackages = "com.phitrading.exchange.integration")
public class PhiTradingExchangeMainApplication {

    public static void main(String[] args) {
        SpringApplication.run(PhiTradingExchangeMainApplication.class, args);
    }

}
