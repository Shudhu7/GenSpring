package com.genspring.repository;

import com.genspring.entity.AIUsageStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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