package com.mapz.domain.domains.user.enums;

import lombok.Getter;

@Getter
public enum UserProvider {
    GOOGLE("GOOGLE"), MAPZ("MAPZ");

    private final String provider;

    UserProvider(String provider) {
        this.provider = provider;
    }
}
