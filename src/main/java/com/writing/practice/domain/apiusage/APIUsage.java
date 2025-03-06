package com.writing.practice.domain.apiusage;

import java.time.LocalDateTime;

public class APIUsage {
    private Long id;
    private LocalDateTime createdAt;
    private String endpoint;
    private int tokensUsed;
    private String model;
    private double temperature;

    public APIUsage(String endpoint, int tokensUsed, String model, double temperature) {
        this.createdAt = LocalDateTime.now();
        this.endpoint = endpoint;
        this.tokensUsed = tokensUsed;
        this.model = model;
        this.temperature = temperature;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getEndpoint() { return endpoint; }
    public int getTokensUsed() { return tokensUsed; }
    public String getModel() { return model; }
    public double getTemperature() { return temperature; }
} 