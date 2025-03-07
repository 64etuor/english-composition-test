package com.writing.practice.service.ai;

import com.writing.practice.service.ai.dto.FeedbackResponseDto;

public interface FeedbackService {
    FeedbackResponseDto getFeedback(String koreanSentence, String userSentence);
} 