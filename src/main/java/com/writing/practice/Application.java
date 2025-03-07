package com.writing.practice;

import com.writing.practice.config.DatabaseConfig;
import com.writing.practice.controller.MenuController;
import com.writing.practice.service.ai.AIServiceFactory;
import com.writing.practice.service.ai.AIServiceFactory.AIProvider;
import com.writing.practice.service.ai.CompositionAIService;

public class Application {
    public static void main(String[] args) {
        DatabaseConfig.initializeDatabase();
        
        String envProvider = System.getenv("AI_PROVIDER");
        String envModel = System.getenv("AI_MODEL");
        
        AIProvider provider = AIProvider.DEEPSEEK;
        if (envProvider != null) {
            try {
                provider = AIProvider.valueOf(envProvider.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("경고: 지원하지 않는 AI 제공자입니다. 기본값(DEEPSEEK)을 사용합니다.");
            }
        }
        
        CompositionAIService aiService = AIServiceFactory.createService(provider);
        
        if (envModel != null) {
            if ((provider == AIProvider.CHATGPT && 
                 (envModel.equals("gpt-3.5-turbo") || envModel.equals("gpt-4"))) ||
                (provider == AIProvider.DEEPSEEK && envModel.equals("deepseek-chat"))) {
                aiService.setModel(envModel);
                
                if (provider == AIProvider.CHATGPT) {
                    aiService.setTemperature(0.7);
                }
            } else {
                System.out.println("경고: 지원하지 않는 모델입니다. 기본 모델을 사용합니다.");
            }
        }
        
        MenuController menuController = new MenuController(aiService);
        menuController.start();
    }
} 
