package com.writing.practice.domain.apiusage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.writing.practice.config.DatabaseConfig;

public class APIUsageRepository {
    
    public void save(APIUsage apiUsage) throws SQLException {
        String sql = """
            INSERT INTO api_usage (
                endpoint, tokens_used, model, temperature
            ) VALUES (?, ?, ?, ?)
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, apiUsage.getEndpoint());
            pstmt.setInt(2, apiUsage.getTokensUsed());
            pstmt.setString(3, apiUsage.getModel());
            pstmt.setDouble(4, apiUsage.getTemperature());
            
            pstmt.executeUpdate();
        }
    }

    public List<APIUsage> findRecentUsage(int days) throws SQLException {
        String sql = """
            SELECT * FROM api_usage 
            WHERE created_at >= datetime('now', ?) 
            ORDER BY created_at DESC
        """;
        
        List<APIUsage> usages = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "-" + days + " days");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    APIUsage usage = new APIUsage(
                        rs.getString("endpoint"),
                        rs.getInt("tokens_used"),
                        rs.getString("model"),
                        rs.getDouble("temperature")
                    );
                    usage.setId(rs.getLong("id"));
                    usages.add(usage);
                }
            }
        }
        return usages;
    }

    public APIUsageStats getUsageStats(int days) throws SQLException {
        String sql = """
            SELECT 
                COUNT(*) as total_calls,
                SUM(tokens_used) as total_tokens,
                AVG(tokens_used) as avg_tokens_per_call,
                endpoint,
                COUNT(*) as endpoint_calls
            FROM api_usage
            WHERE created_at >= datetime('now', ?)
            GROUP BY endpoint
        """;
        
        List<EndpointStats> endpointStats = new ArrayList<>();
        int totalCalls = 0;
        int totalTokens = 0;
        double avgTokensPerCall = 0;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "-" + days + " days");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    endpointStats.add(new EndpointStats(
                        rs.getString("endpoint"),
                        rs.getInt("endpoint_calls"),
                        rs.getInt("total_tokens")
                    ));
                    totalCalls += rs.getInt("total_calls");
                    totalTokens += rs.getInt("total_tokens");
                    avgTokensPerCall = rs.getDouble("avg_tokens_per_call");
                }
            }
        }
        
        return new APIUsageStats(totalCalls, totalTokens, avgTokensPerCall, endpointStats);
    }

    public record EndpointStats(String endpoint, int calls, int tokens) {}
    
    public record APIUsageStats(
        int totalCalls,
        int totalTokens,
        double avgTokensPerCall,
        List<EndpointStats> endpointStats
    ) {}
} 