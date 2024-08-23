package com.finpro.roomio_backend;

import com.finpro.roomio_backend.config.EnvConfigurationProperties;
import com.finpro.roomio_backend.config.RsaKeyConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({RsaKeyConfigProperties.class, EnvConfigurationProperties.class})
public class RoomioBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoomioBackendApplication.class, args);
	}

}
