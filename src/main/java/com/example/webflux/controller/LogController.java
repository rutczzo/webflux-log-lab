package com.example.webflux.controller;

import com.example.webflux.dto.AlertSummaryResponse;
import com.example.webflux.dto.LogStatsResponse;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Mono;

import com.example.webflux.dto.LogRequest;
import com.example.webflux.service.LogService;

import java.util.List;
import java.util.Map;

@RestController
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @PostMapping("/logs")
    public Mono<String> receiveLog(@RequestBody LogRequest request) {

        logService.processLog(request);

        return Mono.just("ok");
    }

    @GetMapping("/logs/recent")
    public Mono<List<LogRequest>> getRecentLogs() {
        List<LogRequest> logs = logService.getRecentLogs();

        return Mono.just(logs);
    }

    @GetMapping("/logs/stats")
    public Mono<LogStatsResponse> getLogStats() {
        LogStatsResponse stats = logService.getStats();

        return Mono.just(stats);
    }

    @GetMapping("/logs/hosts")
    public Mono<Map<String, Integer>> getLogHosts() {
        return Mono.just(logService.getHostsCounts());
    }

    @GetMapping("/logs/levels")
    public Mono<Map<String, Integer>> getLogLevels() {
        return Mono.just(logService.getLevelsCounts());
    }

    @GetMapping("/logs/failed-logins")
    public Mono<Map<String, Integer>> getFailedLoginHosts() {
        return Mono.just(logService.getFailedLoginHosts());
    }

    @GetMapping("/logs/alert")
    public Mono<List<LogRequest>> getAlerts() {
        return Mono.just(logService.getAlerts());
    }

    @GetMapping("/logs/alert-summary")
    public Mono<AlertSummaryResponse> getAlertSummary() {
        return Mono.just(logService.getLastestAlertSummary());
    }
}