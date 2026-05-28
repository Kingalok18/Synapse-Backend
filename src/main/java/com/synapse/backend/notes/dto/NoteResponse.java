package com.synapse.backend.notes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteResponse {
    private String id;
    private UUID userId;
    private String title;
    private String content;
    private boolean isPinned;
    private boolean isArchived;
    private List<String> tags;
    private CanvasPositionDTO canvasPosition;
    private List<String> connections;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
