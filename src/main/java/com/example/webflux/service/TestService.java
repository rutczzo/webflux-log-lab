package com.example.webflux.service;

import org.springframework.stereotype.Service;

@Service
public class TestService {

    public String logic() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "service logic";
    }
}