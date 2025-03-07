package com.writing.practice.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import com.writing.practice.domain.apiusage.APIUsageRepository;
import com.writing.practice.domain.apiusage.APIUsageRepository.APIUsageStats;
import com.writing.practice.domain.composition.Composition;
import com.writing.practice.domain.composition.CompositionRepository;
import com.writing.practice.service.ai.CompositionAIService;
import com.writing.practice.service.ai.dto.FeedbackResponseDto;
import com.writing.practice.service.ai.dto.SentenceResponseDto;
import com.writing.practice.service.ai.dto.WeaknessAnalysisResponseDto;

public class MenuController {
    private final CompositionAIService aiService;
    private final CompositionRepository compositionRepository;
    private final APIUsageRepository apiUsageRepository;
    private final Scanner scanner;

    // ANSI 색상 코드
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String CYAN = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String RED = "\u001B[31m";

    public MenuController(CompositionAIService aiService) {
        this.aiService = aiService;
        this.compositionRepository = new CompositionRepository();
        this.apiUsageRepository = new APIUsageRepository();
        this.scanner = new Scanner(System.in, "UTF-8");
    }

    public void start() {
        while (true) {
            displayMenu();
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> writingPractice();
                case 2 -> viewAPIUsage();
                case 3 -> weaknessAnalysis();
                case 4 -> setTemperature();
                case 5 -> setAIModel();
                case 6 -> setMaxToken();
                case 0 -> {
                    System.out.println("프로그램을 종료합니다.");
                    return;
                }
                default -> System.out.println("잘못된 선택입니다. 다시 선택해주세요.");
            }
        }
    }

    private void displayMenu() {
        String logo = """
            %s
            ╔═══════════════════════════════════════════════════════════════╗
            ║  %s  Writing Practice Pro  %s                                     ║
            ║  %s  Practice Makes Perfect!  %s                                  ║
            ╚═══════════════════════════════════════════════════════════════╝
            %s""".formatted(CYAN, YELLOW + BOLD, CYAN, GREEN + BOLD, CYAN, RESET);

        String tempStr = String.format("%.1f", aiService.getCurrentTemperature());
        String modelStr = aiService.getCurrentModel();
        String tokenStr = String.format("%d", aiService.getCurrentMaxTokens());
        
        tempStr = String.format("%-5s", tempStr);
        modelStr = String.format("%-15s", modelStr);
        tokenStr = String.format("%-6s", tokenStr);

        String menu = """
            %s┌─────────────────────── Menu Options ──────────────────────────┐%s
                                                                         
               %s1.%s Writing Practice                   
               %s2.%s View API Usage                    
               %s3.%s Weakness Analysis           Last 30 days    
               %s4.%s Set Temperature             Current Temp: %s%s                       
               %s5.%s Set AI Model                Current Model: %s%s                     
               %s6.%s Set Max Token               Max Tokens: %s%s                      
               %s0.%s Exit                                                 
                                                                          
            └───────────────────────────────────────────────────────────────┘
            %s선택하세요: """.formatted(
                BLUE, RESET,
                YELLOW + BOLD, RESET,
                YELLOW + BOLD, RESET,
                YELLOW + BOLD, RESET,
                YELLOW + BOLD, RESET, CYAN + tempStr, RESET,
                YELLOW + BOLD, RESET, CYAN + modelStr, RESET,
                YELLOW + BOLD, RESET, CYAN + tokenStr, RESET,
                RED + BOLD, RESET,
                GREEN + BOLD
            );

        System.out.println(logo);
        System.out.print(menu);
    }

    private void displayCategoryMenu() {
        String categoryMenu = """
            %s
            ┌──────────────────── Select Category ────────────────────┐
            │                                                         │
            │    %s1.%s Normal    - Daily life, hobbies, weather          │
            │    %s2.%s Business  - Office work, meetings, negotiations   │
            │    %s3.%s Romantic  - Relationships, feelings, expressions  │
            │    %s4.%s Playful   - Jokes, humor, casual conversations    │
            │    %s0.%s Back to Main Menu                                 │
            │                                                         │
            └─────────────────────────────────────────────────────────┘
            %s선택: """.formatted(
                BLUE,
                YELLOW + BOLD, RESET,
                YELLOW + BOLD, RESET,
                YELLOW + BOLD, RESET,
                YELLOW + BOLD, RESET,
                RED + BOLD, RESET,
                GREEN + BOLD
            );
        
        System.out.print(categoryMenu);
    }

    private void writingPractice() {
        displayCategoryMenu();
        
        int categoryChoice = scanner.nextInt();
        scanner.nextLine();
        
        if (categoryChoice == 0) return;
        
        String category = switch (categoryChoice) {
            case 1 -> "Normal";
            case 2 -> "Business";
            case 3 -> "Romantic";
            case 4 -> "Playful";
            default -> {
                System.out.println(RED + "잘못된 선택입니다." + RESET);
                yield null;
            }
        };
        
        if (category == null) return;

        try {
            System.out.println("\n=== 복습: 최근 작문 기록 ===");
            List<Composition> recentCompositions = compositionRepository.findRecentCompositions(30);
            
            AtomicBoolean shouldContinueDisplay = new AtomicBoolean(true);
            
            Thread reviewThread = new Thread(() -> {
                try {
                    for (Composition comp : recentCompositions.stream().limit(3).toList()) {
                        if (!shouldContinueDisplay.get()) break;
                        
                        System.out.printf("""
                            
                            %s=== 복습 문장 ===%s
                            작성일: %s%s%s
                            한국어: %s%s%s
                            번역: %s%s%s
                            이상적인 번역: %s%s%s
                            점수: %s%.1f%s
                            
                            """,
                            YELLOW + BOLD, RESET,
                            RESET, comp.getCreatedAt(), RESET,
                            RESET, comp.getKoreanSentence(), RESET,
                            RESET, comp.getUserSentence(), RESET,
                            GREEN, comp.getIdealSentence(), RESET,
                            CYAN, comp.getCompositionScore(), RESET
                        );
                        Thread.sleep(8000);
                    }
                } catch (InterruptedException e) {
                }
            });
            reviewThread.start();

            System.out.println("새로운 문장을 가져오는 중입니다...");
            SentenceResponseDto response = aiService.getRandomSentences(category);
            
            shouldContinueDisplay.set(false);
            reviewThread.interrupt();
            
            List<String> sentences = response.sentences();
            List<Map<String, String>> keywords = response.keywords();

            System.out.println("\n=== 오늘의 번역 과제 ===");
            for (int i = 0; i < sentences.size(); i++) {
                System.out.printf("%d. %s%n", i + 1, sentences.get(i));
            }

            System.out.println("\n각 문장의 번역을 입력해주세요.");
            List<String> translations = new ArrayList<>();
            
            for (int i = 0; i < sentences.size(); i++) {
                System.out.printf("\n%d번 문장 번역: ", i + 1);
                translations.add(scanner.nextLine());
            }

            System.out.println("\n피드백을 요청하는 동안 오늘의 핵심 단어를 복습해보세요!");
            
            Thread keywordThread = new Thread(() -> {
                System.out.println("\n=== 오늘의 핵심 단어 ===");
                keywords.forEach(keyword -> {
                    try {
                        if (!shouldContinueDisplay.get()) return;
                        Thread.sleep(2000);
                        String word = keyword.get("word");
                        String meaning = keyword.get("meaning");
                        System.out.printf("\n%s%s%s: %s%s%s", 
                            YELLOW + BOLD, word, RESET,
                            CYAN, meaning, RESET);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            });
            keywordThread.start();

            Thread feedbackThread = new Thread(() -> {
                try {
                    System.out.print("\r⠋ 피드백 분석 중...");
                    List<FeedbackResponseDto> feedbacks = new ArrayList<>();
                    for (int i = 0; i < sentences.size(); i++) {
                        feedbacks.add(aiService.getFeedback(sentences.get(i), translations.get(i)));
                    }
                    
                    shouldContinueDisplay.set(false);
                    keywordThread.interrupt();
                    
                    System.out.println("\n=== 피드백 결과 ===");
                    for (int i = 0; i < sentences.size(); i++) {
                        FeedbackResponseDto feedback = feedbacks.get(i);
                        
                        System.out.printf("""
                            
                            === %d번 문장 ===
                            원문: %s%s%s
                            번역: %s%s%s
                            이상적인 번역: %s%s%s
                            점수: %.1f
                            피드백:
                            %s%s%s
                            """,
                            i + 1,
                            RESET, sentences.get(i), RESET,
                            RESET, translations.get(i), RESET,
                            GREEN, feedback.idealSentence(), RESET,
                            feedback.score(),
                            GREEN, formatFeedback(feedback.feedback()), RESET
                        );
                        
                        Composition composition = new Composition(
                            category,
                            sentences.get(i),
                            translations.get(i),
                            feedback.idealSentence(),
                            feedback.feedback(),
                            feedback.score(),
                            aiService.getCurrentModel(),
                            aiService.getCurrentTemperature()
                        );
                        compositionRepository.save(composition);

                        if (i < sentences.size() - 1) {
                            System.out.println("\n다음 피드백을 보려면 Enter 키를 누르세요...");
                            scanner.nextLine();
                        }
                    }
                } catch (Exception e) {
                    System.out.println("\n피드백 처리 중 오류가 발생했습니다: " + e.getMessage());
                }
            });
            feedbackThread.start();

            feedbackThread.join();
            keywordThread.join();

            System.out.println("\n\n메인 메뉴로 돌아가려면 Enter 키를 누르세요...");
            scanner.nextLine();

        } catch (Exception e) {
            System.out.println("오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void viewAPIUsage() {
        try {
            System.out.println("\n=== API 사용량 통계 ===");
            
            System.out.println("\n[최근 30일 통계]");
            printUsageStats(30);
            
            System.out.println("\n[전체 통계]");
            printUsageStats(36500);
            
            System.out.println("\n메인 메뉴로 돌아가려면 Enter 키를 누르세요...");
            scanner.nextLine();
            
        } catch (Exception e) {
            System.out.println("통계 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private void printUsageStats(int days) throws SQLException {
        APIUsageStats stats = apiUsageRepository.getUsageStats(days);
        
        System.out.printf("""
            총 API 호출 횟수: %d회
            총 토큰 사용량: %d tokens
            호출당 평균 토큰: %.1f tokens
            
            엔드포인트별 통계:
            """, 
            stats.totalCalls(),
            stats.totalTokens(),
            stats.avgTokensPerCall()
        );
        
        stats.endpointStats().forEach(endpoint -> 
            System.out.printf("- %s: %d회 호출, %d tokens 사용%n",
                endpoint.endpoint(),
                endpoint.calls(),
                endpoint.tokens()
            )
        );
    }

    private void weaknessAnalysis() {
        try {
            List<String> recentSentences = compositionRepository.findRecentUserSentences(30);
            if (recentSentences.isEmpty()) {
                System.out.println("분석할 작문 기록이 없습니다.");
                return;
            }

            System.out.println("\n약점을 분석하는 중입니다. 잠시만 기다려주세요...");
            
            Thread analysisThread = new Thread(() -> {
                try {
                    WeaknessAnalysisResponseDto analysis = aiService.analyzeWeakness(recentSentences);
                    
                    System.out.println("\n=== 약점 분석 결과 ===");
                    System.out.println("\n발견된 약점:");
                    analysis.weaknesses().forEach(weakness -> System.out.println("- " + weakness));
                    
                    System.out.println("\n개선 제안:");
                    analysis.improvements().forEach(improvement -> System.out.println("- " + improvement));
                } catch (Exception e) {
                    System.out.println("\n분석 중 오류가 발생했습니다: " + e.getMessage());
                }
            });
            analysisThread.start();

            String[] frames = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};
            int frameIndex = 0;
            while (analysisThread.isAlive()) {
                System.out.print("\r" + frames[frameIndex] + " 분석 중...");
                frameIndex = (frameIndex + 1) % frames.length;
                Thread.sleep(100);
            }
            System.out.println();

            analysisThread.join();

            System.out.println("\n메인 메뉴로 돌아가려면 Enter 키를 누르세요...");
            scanner.nextLine();

        } catch (Exception e) {
            System.out.println("오류가 발생했습니다: " + e.getMessage());
        }
    }

    private void setTemperature() {
        System.out.print("Temperature 값을 입력하세요 (0~1.5): ");
        try {
            double temperature = scanner.nextDouble();
            aiService.setTemperature(temperature);
            System.out.println("Temperature가 설정되었습니다.");
        } catch (Exception e) {
            System.out.println("잘못된 입력입니다: " + e.getMessage());
        }
    }

    private void setAIModel() {
        System.out.println("\n=== AI 모델 설정 ===");
        System.out.println("현재 지원되는 모델:");
        System.out.println("1. Deepseek 모델:");
        System.out.println("   - deepseek-chat");
        System.out.println("2. ChatGPT 모델:");
        System.out.println("   - gpt-3.5-turbo");
        System.out.println("   - gpt-4");
        
        System.out.printf("\n현재 모델: %s%s%s%n", CYAN, aiService.getCurrentModel(), RESET);
        System.out.print("사용할 모델을 입력하세요: ");
        String model = scanner.nextLine().trim().toLowerCase();
        
        List<String> validDeepseekModels = List.of("deepseek-chat");
        List<String> validChatGPTModels = List.of("gpt-3.5-turbo", "gpt-4");
        
        if (model.isEmpty() || 
            (!validDeepseekModels.contains(model) && !validChatGPTModels.contains(model))) {
            System.out.println(RED + "유효하지 않은 모델입니다. 현재 모델을 유지합니다." + RESET);
            return;
        }
        
        aiService.setModel(model);
        
        if (validChatGPTModels.contains(model) && Math.abs(aiService.getCurrentTemperature() - 1.3) < 0.1) {
            aiService.setTemperature(0.7);
            System.out.println(YELLOW + "ChatGPT 모델에 적합한 temperature 값(0.7)으로 자동 조정되었습니다." + RESET);
        }
        
        System.out.println(GREEN + "AI 모델이 성공적으로 설정되었습니다." + RESET);
    }

    private void setMaxToken() {
        System.out.print("최대 토큰 수를 입력하세요 (200~2000): ");
        try {
            int maxTokens = scanner.nextInt();
            aiService.setMaxTokens(maxTokens);
            System.out.println("최대 토큰 수가 설정되었습니다.");
        } catch (Exception e) {
            System.out.println("잘못된 입력입니다: " + e.getMessage());
        }
    }

    private String formatFeedback(String feedback) {
        StringBuilder formatted = new StringBuilder();
        String[] words = feedback.split(" ");
        int lineLength = 0;
        
        for (String word : words) {
            if (lineLength + word.length() > 70) {
                formatted.append("\n");
                lineLength = 0;
            }
            formatted.append(word).append(" ");
            lineLength += word.length() + 1;
        }
        
        return formatted.toString().trim().replaceAll("\n", "\n    ");
    }
} 