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

import io.github.cdimascio.dotenv.Dotenv;

public class DeepseekChatStrategy implements AIService {
    private final String apiKey;
    private final HttpClient client;
    private final ObjectMapper mapper;
    private final APIUsageRepository apiUsageRepository;
    private String model = "deepseek-chat";
    private double temperature = 1.3;
    private int maxTokens = 1000;

    public DeepseekChatStrategy() {
        this.apiKey = Dotenv.load().get("API_KEY");
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        this.apiUsageRepository = new APIUsageRepository();
    }

    private String makeApiCall(String endpoint, String prompt) {
        try {
            ArrayNode messages = mapper.createArrayNode()
                .add(mapper.createObjectNode()
                    .put("role", "system")
                    .put("content", "You are a helpful assistant that always responds in valid JSON format. Do not include any markdown formatting or backticks in your response."))
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
                .uri(URI.create("https://api.deepseek.com/chat/completions"))
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
            
            // 응답에서 JSON 부분만 추출하고 유효한 JSON인지 확인
            content = content.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
            try {
                // 응답이 유효한 JSON인지 확인
                mapper.readTree(content);
            } catch (Exception e) {
                throw new RuntimeException("API가 유효하지 않은 JSON을 반환했습니다.");
            }
            
            // 토큰 사용량 저장
            int tokensUsed = jsonResponse.path("usage").path("total_tokens").asInt();
            APIUsage usage = new APIUsage(endpoint, tokensUsed, model, temperature);
            apiUsageRepository.save(usage);
            
            return content;

        } catch (Exception e) {
            System.err.println("API 호출 중 오류: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("API 호출 중 오류 발생: " + e.getMessage(), e);
        }
    }

    @Override
    public SentenceResponse getRandomSentences(String category) {
        String prompt = String.format("""
            You are a Korean language teacher. Generate 3 interesting and diverse Korean sentences (intermediate level).
            Do not include English translations in the sentences.
            
            Requirements for sentences:
            1. Each sentence should have different grammar patterns and vocabulary level
            2. Mix both casual and formal expressions naturally
            3. Use diverse tenses and sentence structures
            4. Make the content interesting and engaging
            5. Category inspiration (but not limited to):
               - %s category can include any topics that fit the mood               
               - Create unexpected and creative combinations of subjects
               - Use modern and relevant contexts
            
            Also provide 16 key vocabulary words that are actually used in the sentences or are closely related to the context.
            Include a mix of nouns, verbs, adjectives, and adverbs.
            
            Your response must be a valid JSON object with exactly these fields:
            {
                "sentences": ["한국어 문장1", "한국어 문장2", "한국어 문장3"],
                "keywords": [
                    {"word": "한국어 단어1", "meaning": "English meaning 1"},
                    {"word": "한국어 단어2", "meaning": "English meaning 2"},
                    ... (16 keywords total)
                ]
            }
            """, category);
        
        try {
            String response = makeApiCall("random_sentences", prompt);
            ObjectNode jsonResponse = mapper.readValue(response, ObjectNode.class);
            
            List<String> sentences = new ArrayList<>();
            jsonResponse.path("sentences").forEach(node -> sentences.add(node.asText()));
            
            List<Map<String, String>> keywords = new ArrayList<>();
            jsonResponse.path("keywords").forEach(node -> {
                Map<String, String> keyword = new HashMap<>();
                keyword.put(node.path("word").asText(), node.path("meaning").asText());
                keywords.add(keyword);
            });
            
            return new SentenceResponse(sentences, keywords);
        } catch (Exception e) {
            System.err.println("문장 생성 중 오류: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("문장 생성 중 오류 발생: " + e.getMessage(), e);
        }
    }

    @Override
    public FeedbackResponse getFeedback(String koreanSentence, String userSentence) {
        String prompt = String.format("""
            Evaluate the English translation of the Korean sentence and respond in this exact JSON format:
            {
                "idealSentence": "the ideal English translation",
                "feedback": "detailed feedback about the translation",
                "score": 85
            }
            
            Korean sentence: %s
            User's translation: %s
            """, koreanSentence, userSentence);
        
        try {
            String response = makeApiCall("get_feedback", prompt);
            ObjectNode jsonResponse = mapper.readValue(response, ObjectNode.class);
            
            return new FeedbackResponse(
                jsonResponse.path("idealSentence").asText(),
                jsonResponse.path("feedback").asText(),
                jsonResponse.path("score").asDouble()
            );
        } catch (Exception e) {
            throw new RuntimeException("피드백 생성 중 오류 발생: " + e.getMessage(), e);
        }
    }

    @Override
    public WeaknessAnalysisResponse analyzeWeakness(List<String> recentCompositions) {
        String prompt = String.format("""
            Analyze these recent English translations and respond in this exact JSON format,
            Weaknesses and suggestions should be specific :
            {
                "weaknesses": ["weakness1", "weakness2", ...],
                "improvements": ["suggestion1", "suggestion2", ...]
            }
            
            Recent translations:
            %s
            """, String.join("\n", recentCompositions));
        
        try {
            String response = makeApiCall("analyze_weakness", prompt);
            ObjectNode jsonResponse = mapper.readValue(response, ObjectNode.class);
            
            List<String> weaknesses = new ArrayList<>();
            jsonResponse.path("weaknesses").forEach(node -> weaknesses.add(node.asText()));
            
            List<String> improvements = new ArrayList<>();
            jsonResponse.path("improvements").forEach(node -> improvements.add(node.asText()));
            
            return new WeaknessAnalysisResponse(weaknesses, improvements);
        } catch (Exception e) {
            throw new RuntimeException("약점 분석 중 오류 발생: " + e.getMessage(), e);
        }
    }

    @Override
    public void setModel(String model) {
        this.model = model;
    }

    @Override
    public void setTemperature(double temperature) {
        if (temperature < 0 || temperature > 1.5) {
            throw new IllegalArgumentException("Temperature must be between 0 and 1.5");
        }
        this.temperature = temperature;
    }

    @Override
    public void setMaxTokens(int maxTokens) {
        if (maxTokens < 200 || maxTokens > 2000) {
            throw new IllegalArgumentException("MaxTokens must be between 200 and 2000");
        }
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