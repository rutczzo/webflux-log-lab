package com.example.webflux.dto;

public class AlertSummaryResponse {

    private int total;
    private int errorCount;

    public AlertSummaryResponse() {
    }

    public AlertSummaryResponse(int total, int errorCount) {
        this.total = total;
        this.errorCount = errorCount;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }
}