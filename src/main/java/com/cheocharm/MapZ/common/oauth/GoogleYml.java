package com.cheocharm.MapZ.common.oauth;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.ConstructorBinding;


@Getter
@ConstructorBinding
@ConfigurationPropertiesScan
@ConfigurationProperties(prefix = "google")
public class GoogleYml {
    private final String client_id;

    public GoogleYml(String client_id) {
        this.client_id = client_id;
    }
}
