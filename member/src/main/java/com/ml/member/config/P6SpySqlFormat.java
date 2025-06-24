package com.ml.member.config;

import com.p6spy.engine.common.P6Util;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
public class P6SpySqlFormat implements MessageFormattingStrategy {

    @Override
    public String formatMessage(int connectionId, String now, long elapsed,
                                String category, String prepared, String sql, String url) {
        if (sql.trim().isEmpty()) return "";

        return String.format("[INFO] [SQL COST] %d ms | [SQL] %s | [Location] %s",
                elapsed, sql.replaceAll("\\s+", " "),
                getStackTrace());
    }

    private String getStackTrace() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : trace) {
            if (element.getClassName().startsWith("com.ml.member")
                    && !element.getClassName().contains("P6Spy")) {
                return element.getClassName() + ":" + element.getLineNumber();
            }
        }
        return "Unknown Location";
    }
}