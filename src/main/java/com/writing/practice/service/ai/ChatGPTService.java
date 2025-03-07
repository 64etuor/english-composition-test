package com.writing.practice.service.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.writing.practice.domain.apiusage.APIUsage;
import com.writing.practice.domain.apiusage.APIUsageRepository;
import com.writing.practice.service.ai.dto.FeedbackResponseDto;
import com.writing.practice.service.ai.dto.SentenceResponseDto;
import com.writing.practice.service.ai.dto.WeaknessAnalysisResponseDto;

import io.github.cdimascio.dotenv.Dotenv;

public class ChatGPTService implements CompositionAIService {
    private final String apiKey;
    private final HttpClient client;
    private final ObjectMapper mapper;
    private final APIUsageRepository apiUsageRepository;
    private String model = "gpt-3.5-turbo";
    private double temperature = 0.7;
    private int maxTokens = 1000;

    public ChatGPTService() {
        this.apiKey = Dotenv.load().get("OPENAI_API_KEY");
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        this.apiUsageRepository = new APIUsageRepository();
    }

    private String makeApiCall(String endpoint, String prompt) {
        try {
            ArrayNode messages = mapper.createArrayNode()
                .add(mapper.createObjectNode()
                    .put("role", "system")
                    .put("content", "You are a helpful assistant that always responds in valid JSON format."))
                .add(mapper.createObjectNode()
                    .put("role", "user")
                    .put("content", prompt));

            ObjectNode requestBody = mapper.createObjectNode()
                .put("model", model)
                .put("temperature", temperature)
                .put("max_tokens", maxTokens)
                .set("messages", messages);

            String jsonRequest = mapper.writeValueAsString(requestBody);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new RuntimeException("API 호출 실패: " + response.body());
            }

            ObjectNode jsonResponse = mapper.readValue(response.body(), ObjectNode.class);
            String content = jsonResponse.path("choices").path(0).path("message").path("content").asText();
            
            content = content.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
            mapper.readTree(content);
            
            int tokensUsed = jsonResponse.path("usage").path("total_tokens").asInt();
            APIUsage usage = new APIUsage(endpoint, tokensUsed, model, temperature);
            apiUsageRepository.save(usage);
            
            return content;

        } catch (Exception e) {
            throw new RuntimeException("API 호출 중 오류 발생: " + e.getMessage(), e);
        }
    }

    @Override
    public SentenceResponseDto getRandomSentences(String category) {
        String prompt = String.format("""
            As a Korean language teacher, create 3 Korean sentences and vocabulary for the '%s' category.
            
            Category specifications:
            Normal:
            - Focus: Daily activities, hobbies, weather
            - Style: Casual but polite
            - Context: Everyday situations
            
            Business:
            - Focus: Office work, meetings, professional interactions
            - Style: Formal and respectful
            - Context: Workplace situations
            
            Romantic:
            - Focus: Relationships, emotions, personal feelings
            - Style: Gentle and expressive
            - Context: Dating and relationship scenarios
            
            Playful:
            - Focus: Fun, humor, entertainment
            - Style: Casual and light-hearted
            - Context: Friend gatherings, jokes, casual chats
            
            Requirements:
            1. Sentences must be perfectly suited for the '%s' category
            2. Use natural, contemporary Korean
            3. Include category-specific expressions
            4. Match the formality level to the category
            
            Provide 16 vocabulary words that are:
            1. Used in the sentences or highly relevant to the category
            2. Essential for the specific category
            3. Balanced mix of word types (nouns, verbs, adjectives)
            
            Respond in this JSON format:
            {
                "sentences": ["한국어 문장1", "한국어 문장2", "한국어 문장3"],
                "keywords": [
                    {"word": "회의", "meaning": "meeting"},
                    {"word": "발표하다", "meaning": "to present"},
                    ... (16 keywords total)
                ]
            }
            
            Rules:
            - Korean words in "word" field
            - English translations in "meaning" field
            - All content must be relevant to the %s category
            - Use vocabulary appropriate for intermediate level
            """, category, category, category);
        
        String content = makeApiCall("random-sentences", prompt);
        
        try {
            var jsonNode = mapper.readTree(content);
            List<String> sentences = new ArrayList<>();
            jsonNode.path("sentences").forEach(node -> sentences.add(node.asText()));
            
            List<Map<String, String>> keywords = new ArrayList<>();
            jsonNode.path("keywords").forEach(node -> {
                Map<String, String> keyword = new HashMap<>();
                keyword.put("word", node.path("word").asText());
                keyword.put("meaning", node.path("meaning").asText());
                keywords.add(keyword);
            });
            
            return new SentenceResponseDto(sentences, keywords);
        } catch (Exception e) {
            throw new RuntimeException("응답 파싱 중 오류 발생", e);
        }
    }

    @Override
    public FeedbackResponseDto getFeedback(String koreanSentence, String userSentence) {
        String prompt = String.format("""
            Act as an English writing tutor. Evaluate this English translation:
            
            Korean: %s
            Student's English: %s
            
            Provide feedback in this JSON format:
            {
                "idealSentence": "The best possible English translation",
                "feedback": "Detailed feedback about grammar, vocabulary, and natural expression",
                "score": 85
            }
            
            Be encouraging and constructive in your feedback.
            """, koreanSentence, userSentence);
        
        String content = makeApiCall("feedback", prompt);
        
        try {
            var jsonNode = mapper.readTree(content);
            return new FeedbackResponseDto(
                jsonNode.path("idealSentence").asText(),
                jsonNode.path("feedback").asText(),
                jsonNode.path("score").asDouble()
            );
        } catch (Exception e) {
            throw new RuntimeException("응답 파싱 중 오류 발생", e);
        }
    }

    @Override
    public WeaknessAnalysisResponseDto analyzeWeakness(List<String> recentCompositions) {
        if (recentCompositions == null || recentCompositions.isEmpty()) {
            List<String> defaultWeaknesses = List.of("최근 작문 내역이 없어 약점을 분석할 수 없습니다.");
            List<String> defaultImprovements = List.of("영작 연습을 더 많이 하면 약점 분석이 가능합니다.");
            return new WeaknessAnalysisResponseDto(defaultWeaknesses, defaultImprovements);
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < recentCompositions.size(); i++) {
            sb.append((i + 1) + ". " + recentCompositions.get(i) + "\n");
        }
        
        String prompt = String.format("""
            Act as an English writing tutor. Analyze these recent English compositions for weaknesses:
            
            Recent compositions:
            %s
            
            Provide analysis in this JSON format:
            {
                "weaknesses": [
                    "Major weakness 1",
                    "Major weakness 2",
                    ...
                ],
                "improvements": [
                    "Specific improvement suggestion 1",
                    "Specific improvement suggestion 2",
                    ...
                ]
            }
            
            Focus on patterns and recurring issues. Be constructive and specific in suggestions.
            """, sb.toString());
        
        String content = makeApiCall("weakness-analysis", prompt);
        
        try {
            var jsonNode = mapper.readTree(content);
            List<String> weaknesses = new ArrayList<>();
            jsonNode.path("weaknesses").forEach(node -> weaknesses.add(node.asText()));
            
            List<String> improvements = new ArrayList<>();
            jsonNode.path("improvements").forEach(node -> improvements.add(node.asText()));
            
            return new WeaknessAnalysisResponseDto(weaknesses, improvements);
        } catch (Exception e) {
            throw new RuntimeException("응답 파싱 중 오류 발생", e);
        }
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