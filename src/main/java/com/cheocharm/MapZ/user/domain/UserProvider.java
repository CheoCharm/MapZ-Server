package com.cheocharm.MapZ.user.domain;

import lombok.Getter;

@Getter
public enum UserProvider {
    GOOGLE("GOOGLE"), MAPZ("MAPZ");

    private final String provider;

    UserProvider(String provider) {
        this.provider = provider;
    }
}
