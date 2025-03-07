package com.writing.practice.service.ai;

public interface AIConfigurationService {
    void setModel(String model);
    void setTemperature(double temperature);
    void setMaxTokens(int maxTokens);
    String getCurrentModel();
    double getCurrentTemperature();
    int getCurrentMaxTokens();
} 