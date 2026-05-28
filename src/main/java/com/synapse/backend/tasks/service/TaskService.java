package com.synapse.backend.tasks.service;

import com.synapse.backend.tasks.dto.TaskRequest;
import com.synapse.backend.tasks.dto.TaskResponse;
import com.synapse.backend.tasks.model.TaskPriority;
import com.synapse.backend.tasks.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TaskService {
    TaskResponse createTask(TaskRequest request, UUID userId);
    TaskResponse updateTask(UUID id, TaskRequest request, UUID userId);
    void deleteTask(UUID id, UUID userId);
    TaskResponse getTaskById(UUID id, UUID userId);
    Page<TaskResponse> getAllTasks(UUID userId, TaskStatus status, TaskPriority priority, Pageable pageable);
    TaskResponse completeTask(UUID id, UUID userId);
    List<TaskResponse> getUpcomingTasks(UUID userId);
    List<TaskResponse> searchTasks(String query, UUID userId);
    List<TaskResponse> getRecentTasks(UUID userId);
}
