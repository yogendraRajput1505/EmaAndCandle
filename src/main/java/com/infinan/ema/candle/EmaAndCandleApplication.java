package com.infinan.ema.candle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.infinan.common", "com.infinan.common.utils", "com.infinan.ema.candle"})
@EnableJpaRepositories(basePackages = {"com.infinan.common.repo", "com.infinan.ema.candle.repo"})
@EntityScan(basePackages = {"com.infinan.common.entity", "com.infinan.ema.candle.entity"})
public class EmaAndCandleApplication {
	public static void main(String[] args) {
		SpringApplication.run(EmaAndCandleApplication.class, args);
	}

}
