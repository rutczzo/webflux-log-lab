package com.example.webflux.service;

import com.example.webflux.analyzer.LogAnalyzer;
import com.example.webflux.dto.AlertSummaryResponse;
import com.example.webflux.dto.LogStatsResponse;
import org.springframework.stereotype.Service;
import com.example.webflux.dto.LogRequest;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import jakarta.annotation.PostConstruct;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class LogService {

    private final Queue<LogRequest> logBuffer = new ConcurrentLinkedQueue<>();

    private static final int MAX_LOG_SIZE = 100;

    private final LogAnalyzer logAnalyzer;

    private final Sinks.Many<LogRequest> logSink = Sinks.many().multicast().onBackpressureBuffer();

    private volatile AlertSummaryResponse lastestAlertSummary = new AlertSummaryResponse(0, 0);

    public Flux<LogRequest> getLogStream() {
        return logSink.asFlux();
    }

    public LogService(LogAnalyzer logAnalyzer) {
        this.logAnalyzer = logAnalyzer;
    }

    @PostConstruct
    public void init() {
        startLogStreamSubscriber();
        startAlertStreamSubscriber();
        startAlertWindowSubscriber();
    }

    public void startLogStreamSubscriber() {
        getLogStream()
                .subscribe(log -> {
                    System.out.println(
                            "[STREAM] host=" + log.getHost() +
                                    " level=" + log.getLevel() +
                                    " message=" + log.getMessage()
                    );
                });
    }

    public void startAlertStreamSubscriber() {
        getLogStream()
                .filter(log -> {
                    String level = log.getLevel();
                    String message = log.getMessage();

                    return ("WARN".equals(level) || "ERROR".equals(level)
                    || (message != null &&
                            (message.contains("failed login") ||
                            message.contains("suspicious") ||
                            message.contains("access denied"))
                    ));
                })
                .subscribe(log -> {
                    System.out.println(
                            "[ALERT-STREAM] host=" + log.getHost() +
                                    " level=" + log.getLevel() +
                                    " message=" + log.getMessage()
                    );
                });
    }

    public void startAlertWindowSubscriber() {
        getLogStream()
                .filter(log -> {
                    String level = log.getLevel();
                    String message = log.getMessage();

                    return ("WARN".equals(level) || "ERROR".equals(level)
                            || (message != null && (
                            message.contains("failed login")
                                    || message.contains("suspicious")
                                    || message.contains("access denied")
                    )));
                })
                .bufferTimeout(100, Duration.ofSeconds(5))
                .filter(batch -> !batch.isEmpty())
                .subscribe(batch -> {

                    int total = batch.size();
                    int errorCount = 0;

                    for (LogRequest log : batch) {
                        if ("ERROR".equals(log.getLevel())) {
                            errorCount++;
                        }
                    }

                    lastestAlertSummary = new AlertSummaryResponse(total, errorCount);

                    System.out.println(
                            "[ALERT-WINDOW] total=" + total +
                                    " error=" + errorCount
                    );
                });
    }

    public AlertSummaryResponse getLastestAlertSummary() {
        return lastestAlertSummary;
    }

    public void processLog(LogRequest request) {

        String host = request.getHost();
        String level = request.getLevel();
        String message = request.getMessage();

        System.out.println(
                "[LOG] host=" + host +
                        " level=" + level +
                        " message=" + message
        );

        logBuffer.offer(request);

        Sinks.EmitResult result = logSink.tryEmitNext(request);

        if (result.isFailure()) {
            System.out.println("[SINK] emit failed: " + result);
        }

        if (logBuffer.size() > MAX_LOG_SIZE) {
            logBuffer.poll();
        }
    }

    public List<LogRequest> getRecentLogs() {
        return new ArrayList<>(logBuffer);
    }
    public LogStatsResponse getStats() {
        return logAnalyzer.calculateStats(logBuffer);
    }

    public Map<String, Integer> getHostsCounts() {
        return logAnalyzer.countByHost(logBuffer);
    }

    public Map<String, Integer> getLevelsCounts() {
        return logAnalyzer.countByLevel(logBuffer);
    }

    public Map<String, Integer> getFailedLoginHosts() {
        return logAnalyzer.countFailedLoginByHost(logBuffer);
    }

    public List<LogRequest> getAlerts() {
        return logAnalyzer.findAlerts(logBuffer);
    }
}