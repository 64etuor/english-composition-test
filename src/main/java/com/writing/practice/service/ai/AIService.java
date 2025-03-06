package com.writing.practice.service.ai;

import java.util.List;
import java.util.Map;

public interface AIService {
    record SentenceResponse(
        List<String> sentences,
        List<Map<String, String>> keywords
    ) {}

    record FeedbackResponse(
        String idealSentence,
        String feedback,
        double score
    ) {}

    record WeaknessAnalysisResponse(
        List<String> weaknesses,
        List<String> improvements
    ) {}

    SentenceResponse getRandomSentences(String category);
    FeedbackResponse getFeedback(String koreanSentence, String userSentence);
    WeaknessAnalysisResponse analyzeWeakness(List<String> recentCompositions);
    
    void setModel(String model);
    void setTemperature(double temperature);
    void setMaxTokens(int maxTokens);
    
    String getCurrentModel();
    double getCurrentTemperature();
    int getCurrentMaxTokens();
} 