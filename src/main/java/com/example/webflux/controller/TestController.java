package com.example.webflux.controller;

import com.example.webflux.service.TestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class TestController {

    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping("/test")
    public Mono<String> test() {
        System.out.println("Thread = " + Thread.currentThread().getName());
        return Mono.just(testService.logic());
    }
}