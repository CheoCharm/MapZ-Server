package com.cheocharm.MapZ;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class MapZApplication {

	public static void main(String[] args) {
		SpringApplication.run(MapZApplication.class, args);
	}

}
