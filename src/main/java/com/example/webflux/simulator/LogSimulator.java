package com.example.webflux.simulator;

import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

public class LogSimulator {

    public static void main(String[] args) throws InterruptedException {

        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:8080")
                .build();

        String[] hosts = {"server1", "server2", "server3"};
        String[] levels = {"INFO", "WARN", "ERROR"};
        String[] messages = {
                "user login success",
                "failed login",
                "access denied",
                "suspicious command"
        };

        for (int i = 0; i < 100; i++) {

            String host = hosts[i % hosts.length];
            String level = levels[i % levels.length];
            String message = messages[i % messages.length];

            Map<String, String> log = new HashMap<>();
            log.put("host", host);
            log.put("level", level);
            log.put("message", message + " #" + i);

            String response = webClient
                    .post()
                    .uri("/logs")
                    .bodyValue(log)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("response = " + response);

            Thread.sleep(100);
        }
    }
}