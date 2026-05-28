package com.synapse.backend.tasks.service.impl;

import com.synapse.backend.exception.ResourceNotFoundException;
import com.synapse.backend.model.User;
import com.synapse.backend.repository.UserRepository;
import com.synapse.backend.tasks.dto.TaskRequest;
import com.synapse.backend.tasks.dto.TaskResponse;
import com.synapse.backend.tasks.mapper.TaskMapper;
import com.synapse.backend.tasks.model.Task;
import com.synapse.backend.tasks.model.TaskPriority;
import com.synapse.backend.tasks.model.TaskStatus;
import com.synapse.backend.tasks.repository.TaskRepository;
import com.synapse.backend.tasks.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    @Override
    public TaskResponse createTask(TaskRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Task task = taskMapper.toEntity(request);
        task.setUser(user);
        
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.PENDING);
        }
        if (task.getPriority() == null) {
            task.setPriority(TaskPriority.MEDIUM);
        }

        Task saved = taskRepository.save(task);
        return taskMapper.toResponse(saved);
    }

    @Override
    public TaskResponse updateTask(UUID id, TaskRequest request, UUID userId) {
        Task task = getTaskAndValidateOwner(id, userId);
        taskMapper.updateEntityFromRequest(request, task);
        
        // Ensure priority and status are not nulled out
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.PENDING);
        }
        if (task.getPriority() == null) {
            task.setPriority(TaskPriority.MEDIUM);
        }

        Task saved = taskRepository.save(task);
        return taskMapper.toResponse(saved);
    }

    @Override
    public void deleteTask(UUID id, UUID userId) {
        Task task = getTaskAndValidateOwner(id, userId);
        taskRepository.delete(task);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(UUID id, UUID userId) {
        Task task = getTaskAndValidateOwner(id, userId);
        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponse> getAllTasks(UUID userId, TaskStatus status, TaskPriority priority, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<Task> tasks = taskRepository.findAllFiltered(user, status, priority, pageable);
        return tasks.map(taskMapper::toResponse);
    }

    @Override
    public TaskResponse completeTask(UUID id, UUID userId) {
        Task task = getTaskAndValidateOwner(id, userId);
        task.setStatus(TaskStatus.COMPLETED);
        Task saved = taskRepository.save(task);
        return taskMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getUpcomingTasks(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        List<Task> tasks = taskRepository.findUpcomingTasks(user, now);
        return tasks.stream().map(taskMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> searchTasks(String query, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Task> tasks = taskRepository.searchTasks(user, query);
        return tasks.stream().map(taskMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getRecentTasks(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Task> tasks = taskRepository.findTop10ByUserOrderByUpdatedAtDesc(user);
        return tasks.stream().map(taskMapper::toResponse).toList();
    }

    private Task getTaskAndValidateOwner(UUID id, UUID userId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        if (!task.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Task not found");
        }
        return task;
    }
}
