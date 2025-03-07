package com.writing.practice.service.ai;

public class AIServiceFactory {
    
    public enum AIProvider {
        DEEPSEEK,
        CHATGPT
    }
    
    public static CompositionAIService createDefaultService() {
        return createService(AIProvider.DEEPSEEK);
    }
    
  
    public static CompositionAIService createService(AIProvider provider) {
        return switch (provider) {
            case DEEPSEEK -> new DeepseekService();
            case CHATGPT -> new ChatGPTService();
        };
    }
    
    public static CompositionAIService createServiceWithConfig(AIProvider provider, String model, double temperature) {
        CompositionAIService service = createService(provider);
        service.setModel(model);
        service.setTemperature(temperature);
        return service;
    }
} 