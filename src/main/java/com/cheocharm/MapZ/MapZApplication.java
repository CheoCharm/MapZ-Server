package com.cheocharm.MapZ;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@ConfigurationPropertiesScan("com.cheocharm.MapZ.common.oauth")
@EnableJpaAuditing
@SpringBootApplication
public class MapZApplication {

	public static void main(String[] args) {
		SpringApplication.run(MapZApplication.class, args);
	}

}
