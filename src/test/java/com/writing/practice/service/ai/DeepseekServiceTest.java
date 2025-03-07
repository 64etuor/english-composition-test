package com.writing.practice.service.ai;

import java.util.List;
import java.util.Map;

import com.writing.practice.service.ai.dto.FeedbackResponseDto;
import com.writing.practice.service.ai.dto.SentenceResponseDto;
import com.writing.practice.service.ai.dto.WeaknessAnalysisResponseDto;

public class DeepseekServiceTest implements CompositionAIService {
    private String model = "deepseek-chat";
    private double temperature = 1.3;
    private int maxTokens = 1000;

    @Override
    public SentenceResponseDto getRandomSentences(String category) {
        List<String> sentences = List.of(
            "테스트용 한국어 문장 1입니다.",
            "테스트용 한국어 문장 2입니다.",
            "테스트용 한국어 문장 3입니다."
        );
        
        List<Map<String, String>> keywords = List.of(
            Map.of("word", "테스트", "meaning", "test"),
            Map.of("word", "문장", "meaning", "sentence"),
            Map.of("word", "한국어", "meaning", "Korean language")
        );
        
        return new SentenceResponseDto(sentences, keywords);
    }

    @Override
    public FeedbackResponseDto getFeedback(String koreanSentence, String userSentence) {
        return new FeedbackResponseDto(
            "This is a test ideal sentence",
            "This is a test feedback message",
            90.0
        );
    }

    @Override
    public WeaknessAnalysisResponseDto analyzeWeakness(List<String> recentCompositions) {
        List<String> weaknesses = List.of("테스트용 약점 1", "테스트용 약점 2");
        List<String> improvements = List.of("테스트용 개선점 1", "테스트용 개선점 2");
        return new WeaknessAnalysisResponseDto(weaknesses, improvements);
    }

    @Override
    public void setModel(String model) {
        this.model = model;
    }

    @Override
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    @Override
    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    @Override
    public String getCurrentModel() {
        return model;
    }

    @Override
    public double getCurrentTemperature() {
        return temperature;
    }

    @Override
    public int getCurrentMaxTokens() {
        return maxTokens;
    }
} 