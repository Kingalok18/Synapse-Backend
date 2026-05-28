package com.synapse.backend.notes.service;

import com.synapse.backend.notes.dto.CanvasPositionDTO;
import com.synapse.backend.notes.dto.NoteRequest;
import com.synapse.backend.notes.dto.NoteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface NoteService {

    NoteResponse createNote(NoteRequest request, UUID userId);

    NoteResponse updateNote(String id, NoteRequest request, UUID userId);

    void deleteNote(String id, UUID userId);

    NoteResponse getNoteById(String id, UUID userId);

    Page<NoteResponse> getAllNotes(UUID userId, Boolean isPinned, Boolean isArchived, String tag, Pageable pageable);

    List<NoteResponse> searchNotes(String query, UUID userId);

    NoteResponse setPinned(String id, Boolean pinned, UUID userId);

    NoteResponse setArchived(String id, Boolean archived, UUID userId);

    NoteResponse updateCanvasPosition(String id, CanvasPositionDTO position, UUID userId);

    NoteResponse updateConnections(String id, List<String> connectionIds, UUID userId);

    List<NoteResponse> getRecentNotes(UUID userId);
}
