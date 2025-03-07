package com.writing.practice.service.ai.dto;

import java.util.List;
import java.util.Map;

public record SentenceResponseDto(
    List<String> sentences,
    List<Map<String, String>> keywords
) {} 