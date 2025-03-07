package com.writing.practice.service.ai.dto;

public record FeedbackResponseDto(
    String idealSentence,
    String feedback,
    double score
) {} 