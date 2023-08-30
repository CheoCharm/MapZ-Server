package com.mapz.api.common.oauth;

import lombok.Getter;

@Getter
public enum OauthUrl {
    GOOGLE("https://oauth2.googleapis.com/tokeninfo");

    private final String url;

    OauthUrl(String url) {
        this.url = url;
    }
}
