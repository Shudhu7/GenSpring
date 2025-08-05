package com.genspring.repository;

import com.genspring.entity.AIConversation;
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