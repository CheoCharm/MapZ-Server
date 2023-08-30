package com.mapz.api.common.util;

import java.util.Random;

public class RandomUtils {

    public static String makeRandomNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            sb.append(random.nextInt(10));
        }

        return sb.toString();
    }
}
