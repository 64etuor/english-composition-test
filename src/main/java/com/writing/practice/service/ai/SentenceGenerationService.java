package com.writing.practice.service.ai;

import com.writing.practice.service.ai.dto.SentenceResponseDto;

public interface SentenceGenerationService {
    SentenceResponseDto getRandomSentences(String category);
} 