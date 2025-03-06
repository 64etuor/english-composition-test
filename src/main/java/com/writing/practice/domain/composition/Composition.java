package com.writing.practice.domain.composition;

import java.time.LocalDateTime;

public class Composition {
    private Long id;
    private LocalDateTime createdAt;
    private String category;
    private String koreanSentence;
    private String userSentence;
    private String idealSentence;
    private String feedback;
    private Double compositionScore;
    private String model;
    private Double temperature;

    // Constructor
    public Composition(String category, String koreanSentence, String userSentence, 
                      String idealSentence, String feedback, Double compositionScore,
                      String model, Double temperature) {
        this.createdAt = LocalDateTime.now();
        this.category = category;
        this.koreanSentence = koreanSentence;
        this.userSentence = userSentence;
        this.idealSentence = idealSentence;
        this.feedback = feedback;
        this.compositionScore = compositionScore;
        this.model = model;
        this.temperature = temperature;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getCategory() { return category; }
    public String getKoreanSentence() { return koreanSentence; }
    public String getUserSentence() { return userSentence; }
    public String getIdealSentence() { return idealSentence; }
    public String getFeedback() { return feedback; }
    public Double getCompositionScore() { return compositionScore; }
    public String getModel() { return model; }
    public Double getTemperature() { return temperature; }
} 