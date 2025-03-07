package com.writing.practice.service.ai.dto;

import java.util.List;

public record WeaknessAnalysisResponseDto(
    List<String> weaknesses,
    List<String> improvements
) {} 