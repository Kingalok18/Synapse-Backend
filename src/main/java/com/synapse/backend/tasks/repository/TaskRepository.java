package com.synapse.backend.tasks.repository;

import com.synapse.backend.model.User;
import com.synapse.backend.tasks.model.Task;
import com.synapse.backend.tasks.model.TaskPriority;
import com.synapse.backend.tasks.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    @Query("SELECT t FROM Task t WHERE t.user = :user " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:priority IS NULL OR t.priority = :priority)")
    Page<Task> findAllFiltered(@Param("user") User user, 
                               @Param("status") TaskStatus status, 
                               @Param("priority") TaskPriority priority, 
                               Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.user = :user " +
           "AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Task> searchTasks(@Param("user") User user, @Param("query") String query);

    @Query("SELECT t FROM Task t WHERE t.user = :user " +
           "AND t.status = 'PENDING' " +
           "AND t.dueDate IS NOT NULL " +
           "AND t.dueDate >= :now " +
           "ORDER BY t.dueDate ASC")
    List<Task> findUpcomingTasks(@Param("user") User user, @Param("now") LocalDateTime now);

    List<Task> findTop10ByUserOrderByUpdatedAtDesc(User user);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.user.id = :userId AND t.status = :status")
    long countByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") TaskStatus status);
}

