package com.synapse.backend.notes.mapper;

import com.synapse.backend.notes.document.NoteDocument;
import com.synapse.backend.notes.dto.CanvasPositionDTO;
import com.synapse.backend.notes.dto.NoteRequest;
import com.synapse.backend.notes.dto.NoteResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface NoteMapper {

    @Mapping(source = "pinned", target = "isPinned")
    @Mapping(source = "archived", target = "isArchived")
    NoteResponse toResponse(NoteDocument note);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    NoteDocument toDocument(NoteRequest request);

    @Mapping(source = "isPinned", target = "pinned")
    @Mapping(source = "isArchived", target = "archived")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateDocumentFromRequest(NoteRequest request, @MappingTarget NoteDocument note);

    CanvasPositionDTO toCanvasPositionDTO(NoteDocument.CanvasPosition position);

    NoteDocument.CanvasPosition toCanvasPosition(CanvasPositionDTO dto);
}
