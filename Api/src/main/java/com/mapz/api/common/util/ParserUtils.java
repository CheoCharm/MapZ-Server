package com.mapz.api.common.util;

import java.util.regex.Pattern;

public class ParserUtils {

    private static final Pattern brReplaceRegex = Pattern.compile("<br\\s*/?>");
    private static final Pattern htmlReplaceRegex = Pattern.compile("<.*?>");

    public static String getTextFromContent(String content) {
        content = brReplaceRegex.matcher(content).replaceAll(" ");
        return htmlReplaceRegex.matcher(content).replaceAll("");
    }
}
