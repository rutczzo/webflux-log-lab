package com.example.webflux.analyzer;

import com.example.webflux.dto.LogRequest;
import com.example.webflux.dto.LogStatsResponse;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class LogAnalyzer {

    public Map<String, Integer> countByHost(Queue<LogRequest> logs) {
        Map<String, Integer> map = new HashMap<>();
        for (LogRequest log : logs) {
            String host = log.getHost();
            map.put(host, map.getOrDefault(host, 0) + 1);
        }

        return map;
    }

    public Map<String, Integer> countByLevel(Queue<LogRequest> logs) {
        Map<String, Integer> map = new HashMap<>();
        for (LogRequest log : logs) {
            String level = log.getLevel();
            map.put(level, map.getOrDefault(level,0) + 1);
        }

        return map;
    }

    public Map<String, Integer> countFailedLoginByHost(Queue<LogRequest> logs) {
        Map<String, Integer> map = new HashMap<>();
        for (LogRequest log : logs) {
            String host = log.getHost();
            String message = log.getMessage();

            if (message != null && message.contains("failed login")) {
                map.put(host, map.getOrDefault(host, 0) + 1);
            }
        }

        return map;
    }

    public LogStatsResponse calculateStats(Queue<LogRequest> logs) {
        int totalCount = 0;
        int infoCount = 0;
        int warnCount = 0;
        int errorCount = 0;
        int failedLoginCount = 0;

        for (LogRequest log : logs) {

            totalCount++;

            if ("INFO".equals(log.getLevel())) infoCount++;
            if ("WARN".equals(log.getLevel())) warnCount++;
            if ("ERROR".equals(log.getLevel())) errorCount++;

            if (log.getMessage() != null &&
                    log.getMessage().contains("failed login"))
                failedLoginCount++;
        }

        String status;

        if (errorCount >= 10) status = "DANGER";
        else if (warnCount >= 10 || failedLoginCount >= 5) status = "WARNING";
        else status = "NORMAL";

        return new LogStatsResponse(
                totalCount,
                infoCount,
                warnCount,
                errorCount,
                failedLoginCount,
                status
        );
    }

    public List<LogRequest> findAlerts(Queue<LogRequest> logs) {
        List<LogRequest> alert = new ArrayList<>();
        for (LogRequest log : logs) {
            String level = log.getLevel();
            String message = log.getMessage();

            boolean isAlert = false;

            if ("WARN".equals(level) || "ERROR".equals(level)) {
                isAlert = true;
            }

            if (message != null && (
                    message.contains("failed login")
                    || message.contains("suspicious")
                    || message.contains("access denied")
            )) {
                isAlert = true;
            }

            if (isAlert) alert.add(log);
        }

        return alert;
    }
}