package com.genspring.repository;

import com.genspring.entity.AIConversation;
import com.genspring.entity.AIUsageStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AIConversationRepository extends JpaRepository<AIConversation, Long> {
    
    List<AIConversation> findByUserIdOrderByCreatedAtDesc(String userId);
    
    List<AIConversation> findByStatusOrderByCreatedAtDesc(String status);
    
    @Query("SELECT c FROM AIConversation c WHERE c.userId = :userId AND c.createdAt >= :fromDate")
    List<AIConversation> findByUserIdAndCreatedAtAfter(
            @Param("userId") String userId, 
            @Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT COUNT(c) FROM AIConversation c WHERE c.userId = :userId AND c.createdAt >= :fromDate")
    Long countByUserIdAndCreatedAtAfter(
            @Param("userId") String userId, 
            @Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT c FROM AIConversation c WHERE c.createdAt BETWEEN :startDate AND :endDate ORDER BY c.createdAt DESC")
    List<AIConversation> findByDateRange(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(c.tokensUsed) FROM AIConversation c WHERE c.userId = :userId AND c.status = 'success'")
    Optional<Long> getTotalTokensUsedByUser(@Param("userId") String userId);
    
    @Query("SELECT AVG(c.processingTimeMs) FROM AIConversation c WHERE c.status = 'success'")
    Optional<Double> getAverageProcessingTime();
    
    @Query("SELECT c.model, COUNT(c) FROM AIConversation c GROUP BY c.model")
    List<Object[]> getUsageByModel();
}

@Repository
public interface AIUsageStatsRepository extends JpaRepository<AIUsageStats, Long> {
    
    Optional<AIUsageStats> findByUserIdAndDate(String userId, LocalDateTime date);
    
    List<AIUsageStats> findByUserIdOrderByDateDesc(String userId);
    
    @Query("SELECT s FROM AIUsageStats s WHERE s.date >= :fromDate ORDER BY s.date DESC")
    List<AIUsageStats> findByDateAfter(@Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT SUM(s.requestsCount) FROM AIUsageStats s WHERE s.date >= :fromDate")
    Optional<Long> getTotalRequestsAfterDate(@Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT SUM(s.tokensUsed) FROM AIUsageStats s WHERE s.date >= :fromDate")
    Optional<Long> getTotalTokensAfterDate(@Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT s.userId, SUM(s.requestsCount) FROM AIUsageStats s WHERE s.date >= :fromDate GROUP BY s.userId ORDER BY SUM(s.requestsCount) DESC")
    List<Object[]> getTopUsersByRequests(@Param("fromDate") LocalDateTime fromDate);
}