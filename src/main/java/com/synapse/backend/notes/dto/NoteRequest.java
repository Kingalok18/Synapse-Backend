package com.synapse.backend.notes.dto;

import com.synapse.backend.notes.validation.ValidTags;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    private String content;

    private Boolean isPinned;

    private Boolean isArchived;

    @ValidTags
    private List<String> tags;

    @Valid
    private CanvasPositionDTO canvasPosition;

    private List<String> connections;
}
