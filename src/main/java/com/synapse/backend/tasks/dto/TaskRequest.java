package com.synapse.backend.tasks.dto;

import com.synapse.backend.tasks.model.TaskPriority;
import com.synapse.backend.tasks.model.TaskStatus;
import com.synapse.backend.tasks.validation.FutureOrToday;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String description;

    @NotNull(message = "Priority is required")
    private TaskPriority priority;

    private TaskStatus status;

    @FutureOrToday
    private LocalDateTime dueDate;
}
