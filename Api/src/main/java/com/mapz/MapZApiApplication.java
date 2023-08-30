package com.mapz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@ConfigurationPropertiesScan("com.mapz.api.common.oauth")
@EnableAspectJAutoProxy
@EnableJpaAuditing
@EntityScan("com.mapz.domain")
@SpringBootApplication
public class MapZApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MapZApiApplication.class, args);
    }
}
