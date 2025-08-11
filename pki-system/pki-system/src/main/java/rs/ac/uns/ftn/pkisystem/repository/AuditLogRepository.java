package rs.ac.uns.ftn.pkisystem.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.pkisystem.entity.AuditLog;
import rs.ac.uns.ftn.pkisystem.entity.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByUser(User user, Pageable pageable);

    Page<AuditLog> findByEventType(String eventType, Pageable pageable);

    Page<AuditLog> findByResourceType(String resourceType, Pageable pageable);

    Page<AuditLog> findBySuccessTrue(Pageable pageable);

    Page<AuditLog> findBySuccessFalse(Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.timestamp BETWEEN :startDate AND :endDate")
    Page<AuditLog> findByTimestampBetween(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.user = :user AND al.timestamp BETWEEN :startDate AND :endDate")
    Page<AuditLog> findByUserAndTimestampBetween(@Param("user") User user,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate,
                                                 Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.eventType = :eventType AND al.timestamp BETWEEN :startDate AND :endDate")
    List<AuditLog> findByEventTypeAndTimestampBetween(@Param("eventType") String eventType,
                                                      @Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.eventType = :eventType AND al.timestamp >= :since")
    long countByEventTypeSince(@Param("eventType") String eventType, @Param("since") LocalDateTime since);

    @Query("SELECT al.eventType, COUNT(al) FROM AuditLog al WHERE al.timestamp >= :since GROUP BY al.eventType")
    List<Object[]> getEventTypeStatistics(@Param("since") LocalDateTime since);

    @Query("SELECT DATE(al.timestamp), COUNT(al) FROM AuditLog al WHERE al.timestamp >= :since GROUP BY DATE(al.timestamp) ORDER BY DATE(al.timestamp)")
    List<Object[]> getDailyActivityStatistics(@Param("since") LocalDateTime since);
}