package com.cheocharm.MapZ.common.util;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class RandomUtils {

    public String makeRandomNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            sb.append(random.nextInt(10));
        }

        return sb.toString();
    }
}
