package com.synapse.backend.tasks.controller;

import com.synapse.backend.dto.ApiResponse;
import com.synapse.backend.security.UserPrincipal;
import com.synapse.backend.tasks.dto.TaskRequest;
import com.synapse.backend.tasks.dto.TaskResponse;
import com.synapse.backend.tasks.model.TaskPriority;
import com.synapse.backend.tasks.model.TaskStatus;
import com.synapse.backend.tasks.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Endpoints for managing user tasks, deadlines, priorities, and completions")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "Create a new task", description = "Creates a task with title, description, priority, and optional due date.")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        TaskResponse response = taskService.createTask(request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Task created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a task", description = "Updates title, description, priority, status, and due date of an existing task.")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        TaskResponse response = taskService.updateTask(id, request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task", description = "Permanently deletes a task from the user's workspace.")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        taskService.deleteTask(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", description = "Retrieves details of a task by its UUID.")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        TaskResponse response = taskService.getTaskById(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Task retrieved successfully", response));
    }

    @GetMapping
    @Operation(summary = "List tasks with pagination, sorting and filtering", description = "Retrieves filtered list of user tasks.")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getAllTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Page<TaskResponse> response = taskService.getAllTasks(userPrincipal.getId(), status, priority, pageable);
        return ResponseEntity.ok(ApiResponse.success("Tasks listed successfully", response));
    }

    @PatchMapping("/complete/{id}")
    @Operation(summary = "Mark a task as completed", description = "Updates status of the task to COMPLETED.")
    public ResponseEntity<ApiResponse<TaskResponse>> completeTask(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        TaskResponse response = taskService.completeTask(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Task completed successfully", response));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming tasks", description = "Retrieves pending tasks with a future due date, sorted by deadline ascending.")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getUpcoming(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<TaskResponse> response = taskService.getUpcomingTasks(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Upcoming tasks retrieved successfully", response));
    }

    @GetMapping("/search")
    @Operation(summary = "Search tasks", description = "Search title and description case-insensitively.")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> searchTasks(
            @RequestParam("q") String query,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<TaskResponse> response = taskService.searchTasks(query, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Search results retrieved successfully", response));
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recently updated tasks", description = "Retrieves the top 10 most recently updated tasks.")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getRecent(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<TaskResponse> response = taskService.getRecentTasks(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Recent tasks retrieved successfully", response));
    }
}
