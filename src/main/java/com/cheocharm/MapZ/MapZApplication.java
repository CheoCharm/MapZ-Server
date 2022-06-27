package com.cheocharm.MapZ;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@ConfigurationPropertiesScan("com.cheocharm.MapZ.common.oauth")
@EnableAspectJAutoProxy
@EnableJpaAuditing
@SpringBootApplication
public class MapZApplication {

	public static void main(String[] args) {
		SpringApplication.run(MapZApplication.class, args);
	}

}
