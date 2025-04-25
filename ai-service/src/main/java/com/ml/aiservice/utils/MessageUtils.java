package com.ml.aiservice.utils;

public class MessageUtils {
    public static String extractText(String raw) {
        if (raw == null) return null;

        int startIdx = raw.indexOf("\"");
        int endIdx = raw.lastIndexOf("\"");

        if (startIdx >= 0 && endIdx > startIdx) {
            return raw.substring(startIdx + 1, endIdx);
        }
        return raw;
    }
}
