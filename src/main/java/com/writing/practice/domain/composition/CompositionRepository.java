package com.writing.practice.domain.composition;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.writing.practice.config.DatabaseConfig;

public class CompositionRepository {
    
    public void save(Composition composition) throws SQLException {
        String sql = """
            INSERT INTO compositions (
                category, korean_sentence, user_sentence, ideal_sentence,
                feedback, composition_score, model, temperature
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, composition.getCategory());
            pstmt.setString(2, composition.getKoreanSentence());
            pstmt.setString(3, composition.getUserSentence());
            pstmt.setString(4, composition.getIdealSentence());
            pstmt.setString(5, composition.getFeedback());
            pstmt.setDouble(6, composition.getCompositionScore());
            pstmt.setString(7, composition.getModel());
            pstmt.setDouble(8, composition.getTemperature());
            
            pstmt.executeUpdate();
        }
    }

    public List<Composition> findRecentCompositions(int days) throws SQLException {
        String sql = """
            SELECT * FROM compositions 
            WHERE created_at >= datetime('now', ?) 
            ORDER BY created_at DESC
        """;
        
        List<Composition> compositions = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "-" + days + " days");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Composition composition = new Composition(
                        rs.getString("category"),
                        rs.getString("korean_sentence"),
                        rs.getString("user_sentence"),
                        rs.getString("ideal_sentence"),
                        rs.getString("feedback"),
                        rs.getDouble("composition_score"),
                        rs.getString("model"),
                        rs.getDouble("temperature")
                    );
                    composition.setId(rs.getLong("id"));
                    compositions.add(composition);
                }
            }
        }
        return compositions;
    }

    public List<String> findRecentUserSentences(int days) throws SQLException {
        String sql = """
            SELECT user_sentence FROM compositions 
            WHERE created_at >= datetime('now', ?) 
            ORDER BY created_at DESC
        """;
        
        List<String> sentences = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "-" + days + " days");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    sentences.add(rs.getString("user_sentence"));
                }
            }
        }
        return sentences;
    }
} 