package com.writing.practice.service.ai;

import java.util.List;

import com.writing.practice.service.ai.dto.WeaknessAnalysisResponseDto;

public interface WeaknessAnalysisService {
    WeaknessAnalysisResponseDto analyzeWeakness(List<String> recentCompositions);
} 