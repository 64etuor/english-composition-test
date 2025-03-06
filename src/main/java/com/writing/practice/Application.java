package com.writing.practice;

import com.writing.practice.config.DatabaseConfig;
import com.writing.practice.controller.MenuController;
import com.writing.practice.service.ai.AIService;
import com.writing.practice.service.ai.DeepseekChatStrategy;

public class Application {
    public static void main(String[] args) {
        DatabaseConfig.initializeDatabase();
        
        AIService aiService = new DeepseekChatStrategy();
        
        MenuController menuController = new MenuController(aiService);
        menuController.start();
    }
} 
