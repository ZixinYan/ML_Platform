package com.ml.agent.config;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;


@Component
public class P6SpySqlFormat implements MessageFormattingStrategy {

    @Override
    public String formatMessage(int connectionId, String now, long elapsed,
                                String category, String prepared, String sql, String url) {
        if (sql == null || sql.trim().isEmpty()) return "";

        return String.format("[SQL耗时] %d ms | [SQL语句] %s | [位置] %s",
                elapsed,
                sql.replaceAll("\\s+", " "),
                getLocation());
    }

    private String getLocation() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getClassName().startsWith("com.ml.agent")
                    && !element.getClassName().contains("P6Spy")) {
                return element.getClassName() + ":" + element.getLineNumber();
            }
        }
        return "Unknown Location";
    }
}
