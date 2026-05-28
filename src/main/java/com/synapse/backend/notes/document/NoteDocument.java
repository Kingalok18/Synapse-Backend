package com.synapse.backend.notes.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "notes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndexes({
    @CompoundIndex(name = "user_tags", def = "{'userId': 1, 'tags': 1}")
})
public class NoteDocument {

    @Id
    private String id;

    @Indexed
    private UUID userId;

    @TextIndexed(weight = 3)
    private String title;

    @TextIndexed(weight = 1)
    private String content;

    private boolean isPinned;
    
    private boolean isArchived;
    
    private boolean deleted;

    @Indexed
    @Builder.Default
    @TextIndexed(weight = 1)
    private List<String> tags = new ArrayList<>();

    @Builder.Default
    private CanvasPosition canvasPosition = new CanvasPosition(0.0, 0.0);

    @Builder.Default
    private List<String> connections = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CanvasPosition {
        private double x;
        private double y;
    }
}
