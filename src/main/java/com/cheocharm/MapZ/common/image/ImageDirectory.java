package com.cheocharm.MapZ.common.image;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageDirectory {
    USER("Mapz/User/"),
    GROUP("Mapz/Group/"),
    DIARY("Mapz/Diary/"),
    ;

    private final String baseDirectory;
}
